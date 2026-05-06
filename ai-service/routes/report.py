from flask import Blueprint, request, jsonify
from services.groq_client import call_groq
from services.cache import get_cache, set_cache
from services.metrics import record_ai_response_time
import json
from datetime import datetime, timezone
import time

report_bp = Blueprint("report", __name__)

def load_prompt(threat, recommendations):
    with open("prompts/report.txt", "r") as f:
        template = f.read()
    return template.replace("{threat}", threat).replace("{recommendations}", recommendations)

@report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    data = request.json

    threat = data.get("threat")
    recommendations = data.get("recommendations")

    if not threat or not recommendations:
        return jsonify({
            "error": "Threat and recommendations required",
            "report": {}
        }), 400

    prompt = load_prompt(threat, recommendations)
    cached_result = get_cache(prompt)

    if cached_result:
        cached_result["cached"] = True
        cached_result["generated_at"] = datetime.now(timezone.utc).isoformat()
        return jsonify(cached_result), 200

    start_time = time.time()
    ai_response = call_groq(prompt)
    record_ai_response_time(time.time() - start_time)

    print("RAW REPORT:", ai_response)

    # Clean markdown
    ai_response = ai_response.strip().replace("```json", "").replace("```", "")

    # Try parsing AI response as JSON
    try:
        parsed = json.loads(ai_response)
        result = {
            "report": parsed,
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": False
        }
        set_cache(prompt, result)
        return jsonify(result)
    except Exception:
        return jsonify({
            "error": "AI returned invalid JSON",
            "raw_response": ai_response,
            "generated_at": datetime.now(timezone.utc).isoformat()
        }), 500