import re
import threading
import os

from dotenv import load_dotenv
from flask import Flask, jsonify, request
from werkzeug.exceptions import HTTPException

try:
    from flask_limiter import Limiter
    from flask_limiter.util import get_remote_address
except ImportError:
    Limiter = None
    get_remote_address = None
    print("Warning: flask-limiter is not installed; rate limiting is disabled.")

try:
    from sentence_transformers import SentenceTransformer
except ImportError:
    SentenceTransformer = None
    print("Warning: sentence-transformers is not installed; embeddings preload is disabled.")

try:
    import chromadb
except Exception:
    chromadb = None
    print("Warning: chromadb is not installed; threat knowledge lookup is disabled.")

load_dotenv()

import routes.describe as describe_route
import routes.recommend as recommend_route
import routes.report as report_route
from routes.describe import describe_bp
from routes.recommend import recommend_bp
from routes.report import report_bp
from services.groq_client import MODEL_NAME
from services.groq_client import call_groq as groq_call
from services.metrics import get_average_response_time_ms, get_uptime_seconds

embedding_model = None
_get_cache = describe_route.get_cache
_set_cache = describe_route.set_cache
_describe_load_prompt = describe_route.load_prompt
_recommend_load_prompt = recommend_route.load_prompt
threat_collection = None
memory_cache = {}

THREAT_DOCUMENTS = [
    {
        "id": "sql-injection",
        "topic": "SQL Injection",
        "text": "SQL Injection happens when unsafe database queries allow attackers to read or change data."
    },
    {
        "id": "xss",
        "topic": "XSS",
        "text": "Cross-site scripting allows attackers to run malicious scripts in a user's browser."
    },
    {
        "id": "phishing",
        "topic": "Phishing",
        "text": "Phishing uses deceptive emails or websites to steal credentials or sensitive information."
    },
    {
        "id": "malware",
        "topic": "Malware",
        "text": "Malware is malicious software designed to damage systems, steal data, or gain access."
    },
    {
        "id": "ransomware",
        "topic": "Ransomware",
        "text": "Ransomware encrypts files and demands payment before restoring access."
    },
    {
        "id": "dos-attack",
        "topic": "DoS attack",
        "text": "A denial-of-service attack overwhelms a service so legitimate users cannot access it."
    },
    {
        "id": "mitm",
        "topic": "Man-in-the-middle",
        "text": "A man-in-the-middle attack intercepts communication between two parties."
    },
    {
        "id": "weak-passwords",
        "topic": "Weak passwords",
        "text": "Weak passwords are easy to guess or crack and can lead to account takeover."
    },
    {
        "id": "open-ports",
        "topic": "Open ports",
        "text": "Open ports expose services to networks and should be limited to required access."
    },
    {
        "id": "data-breach",
        "topic": "Data breach",
        "text": "A data breach exposes confidential, personal, or business data to unauthorized parties."
    }
]


def preload_embeddings():
    global embedding_model

    if SentenceTransformer is None:
        return

    try:
        embedding_model = SentenceTransformer("all-MiniLM-L6-v2")
        print("Sentence-transformers model pre-loaded.")
    except Exception as e:
        embedding_model = None
        print(f"Warning: sentence-transformers preload failed: {e}")


def init_chromadb():
    global threat_collection

    if chromadb is None:
        return

    try:
        client = chromadb.Client()
        threat_collection = client.get_or_create_collection("threat_knowledge")
        threat_collection.upsert(
            ids=[item["id"] for item in THREAT_DOCUMENTS],
            documents=[item["text"] for item in THREAT_DOCUMENTS],
            metadatas=[{"topic": item["topic"]} for item in THREAT_DOCUMENTS]
        )
        print("ChromaDB threat_knowledge collection ready.")
    except Exception as e:
        threat_collection = None
        print(f"Warning: ChromaDB setup failed: {e}")


def get_threat_context(input_text, limit=2):
    if not threat_collection:
        return ""

    try:
        results = threat_collection.query(
            query_texts=[input_text],
            n_results=limit
        )
        documents = results.get("documents", [[]])[0]
        metadatas = results.get("metadatas", [[]])[0]
        context_lines = []

        for index, document in enumerate(documents):
            topic = metadatas[index].get("topic", "Security") if index < len(metadatas) else "Security"
            context_lines.append(f"{topic}: {document}")

        return "\n".join(context_lines)
    except Exception:
        return ""


def add_threat_context(input_text):
    context = get_threat_context(input_text)
    if not context:
        return input_text
    return f"{input_text}\n\nAdditional security context:\n{context}"


def load_describe_prompt_with_context(input_text):
    return _describe_load_prompt(add_threat_context(input_text))


def load_recommend_prompt_with_context(input_text):
    return _recommend_load_prompt(add_threat_context(input_text))


def safe_call_groq(prompt, timeout=10):
    if os.getenv("FORCE_GROQ_FAILURE") == "1":
        return {
            "success": False,
            "error": "AI service temporarily unavailable",
            "is_timeout": False
        }

    try:
        return groq_call(prompt, timeout=timeout or 10)
    except Exception:
        return {
            "success": False,
            "error": "AI service temporarily unavailable",
            "is_timeout": False
        }


def safe_get_cache(key):
    if key in memory_cache:
        cached_value = memory_cache[key].copy()
        cached_value["cached"] = True
        return cached_value
    return None


def safe_set_cache(key, value):
    memory_cache[key] = value.copy() if isinstance(value, dict) else value
    try:
        _set_cache(key, value)
    except Exception:
        return None


describe_route.call_groq = safe_call_groq
recommend_route.call_groq = safe_call_groq
report_route.call_groq = safe_call_groq
describe_route.load_prompt = load_describe_prompt_with_context
recommend_route.load_prompt = load_recommend_prompt_with_context
describe_route.get_cache = safe_get_cache
recommend_route.get_cache = safe_get_cache
report_route.get_cache = safe_get_cache
describe_route.set_cache = safe_set_cache
recommend_route.set_cache = safe_set_cache
report_route.set_cache = safe_set_cache

app = Flask(__name__)

if Limiter and get_remote_address:
    limiter = Limiter(
        app=app,
        key_func=get_remote_address,
        default_limits=["30 per minute"]
    )
else:
    limiter = None

HTML_TAG_RE = re.compile(r"<[^>]*>")
PROTECTED_ENDPOINTS = {"/describe", "/recommend", "/generate-report"}


def sanitize_text(value):
    if not isinstance(value, str):
        return value
    return HTML_TAG_RE.sub("", value).strip()


def validate_text_field(data, field_name):
    value = sanitize_text(data.get(field_name))
    if not value:
        return None, jsonify({"error": "Invalid input"}), 400
    if len(value) < 3:
        return None, jsonify({"error": "Input is too short"}), 400
    if len(value) > 2000:
        return None, jsonify({"error": "Input is too long (max 2000 chars)"}), 400
    return value, None, None


@app.before_request
def validate_and_sanitize_input():
    if request.path not in PROTECTED_ENDPOINTS or request.method != "POST":
        return None

    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({"error": "Invalid input"}), 400

    if request.path in {"/describe", "/recommend"}:
        value, response, status = validate_text_field(data, "input")
        if response:
            return response, status
        data["input"] = value

    if request.path == "/generate-report":
        threat, response, status = validate_text_field(data, "threat")
        if response:
            return response, status

        recommendations, response, status = validate_text_field(data, "recommendations")
        if response:
            return response, status

        data["threat"] = threat
        data["recommendations"] = recommendations

    request._cached_json = (data, data)
    return None


@app.route("/")
def home():
    return "AI Service is running"


@app.route("/health")
def health():
    return jsonify({
        "status": "ok",
        "model": MODEL_NAME,
        "avg_response_time_ms": get_average_response_time_ms(),
        "uptime_seconds": get_uptime_seconds()
    })


app.register_blueprint(describe_bp)
app.register_blueprint(recommend_bp)
app.register_blueprint(report_bp)


@app.after_request
def add_security_headers(response):
    if response.is_json:
        body = response.get_json(silent=True)
        if isinstance(body, dict):
            if "error_reason" in body:
                body["error_reason"] = "AI service temporarily unavailable"
            if "parse_error" in body:
                body["parse_error"] = "Unable to parse AI response"
            if "raw_response" in body:
                body.pop("raw_response", None)
            response.set_data(app.json.dumps(body))

    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    response.headers["Content-Security-Policy"] = "default-src 'self'"
    response.headers["Referrer-Policy"] = "no-referrer"
    response.headers["Permissions-Policy"] = "geolocation=(), microphone=()"
    return response


@app.errorhandler(429)
def rate_limit_exceeded(_):
    return jsonify({"error": "Rate limit exceeded"}), 429


@app.errorhandler(Exception)
def handle_unexpected_error(error):
    if isinstance(error, HTTPException):
        return error
    return jsonify({"error": "Service temporarily unavailable"}), 500


if __name__ == "__main__":
    init_chromadb()

    preload_thread = threading.Thread(target=preload_embeddings, daemon=True)
    preload_thread.start()

    app.run(host="0.0.0.0", port=int(os.getenv("PORT", 5000)), debug=False)
