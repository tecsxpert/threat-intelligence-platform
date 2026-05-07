from flask import Blueprint, request, jsonify
from services.groq_client import call_groq
from services.cache import get_cache, set_cache
from services.metrics import record_ai_response_time
from services.fallback_templates import FALLBACK_DESCRIBE
from datetime import datetime, timezone
import json
import time

describe_bp = Blueprint("describe", __name__)

def load_prompt(input_text):
    with open("prompts/describe.txt", "r") as f:
        template = f.read()
    return template.replace("{input}", input_text)

@describe_bp.route("/describe", methods=["POST"])
def describe():
    data = request.json

    user_input = data.get("input")

    if not user_input:
        return jsonify({"error": "Invalid input"}), 400

    if len(user_input) < 3:
        return jsonify({"error": "Input is too short"}), 400

    if len(user_input) > 2000:
        return jsonify({"error": "Input is too long (max 2000 chars)"}), 400

    prompt = load_prompt(user_input)
    
    # Check cache first - this is fastest way to respond
    cached_result = get_cache(prompt)
    if cached_result:
        cached_result["cached"] = True
        cached_result["generated_at"] = datetime.now(timezone.utc).isoformat()
        return jsonify(cached_result), 200

    # Call Groq API with 10 second timeout for <2s average response
    start_time = time.time()
    ai_response = call_groq(prompt, timeout=10)
    response_time = time.time() - start_time
    record_ai_response_time(response_time)

    # If Groq failed, return fallback response with is_fallback=True
    if not ai_response['success']:
        fallback_result = {
            **FALLBACK_DESCRIBE,  # Use fallback template
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": True,
            "error_reason": ai_response['error']
        }
        set_cache(prompt, fallback_result)
        return jsonify(fallback_result), 200  # Return 200 with fallback, not 500

    # If Groq succeeded, try to parse JSON
    try:
        parsed = json.loads(ai_response['content'])
        result = {
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "data": parsed,
            "is_fallback": False
        }
        set_cache(prompt, result)
        return jsonify(result), 200

    except Exception:
        # If JSON parsing fails, return fallback response
        fallback_result = {
            **FALLBACK_DESCRIBE,
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": True,
            "raw_response": ai_response['content']
        }
        set_cache(prompt, fallback_result)
        return jsonify(fallback_result), 200
