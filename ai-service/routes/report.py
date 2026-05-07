from flask import Blueprint, request, jsonify
from services.groq_client import call_groq
from services.cache import get_cache, set_cache
from services.metrics import record_ai_response_time
from services.fallback_templates import FALLBACK_REPORT
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
            "report": FALLBACK_REPORT,  # Use fallback template
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": True,
            "error_reason": ai_response['error']
        }
        set_cache(prompt, fallback_result)
        return jsonify(fallback_result), 200  # Return 200 with fallback, not 500

    # If Groq succeeded, try to parse JSON
    ai_content = ai_response['content'].strip()
    ai_content = ai_content.replace("```json", "").replace("```", "").strip()

    print("RAW REPORT:", ai_content)

    try:
        parsed = json.loads(ai_content)
        result = {
            "report": parsed,
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": False
        }
        set_cache(prompt, result)
        return jsonify(result), 200

    except Exception as e:
        # If JSON parsing fails, return fallback response
        print("REPORT JSON PARSE ERROR:", str(e))
        
        fallback_result = {
            "report": FALLBACK_REPORT,
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": True,
            "parse_error": str(e),
            "raw_response": ai_content
        }
        set_cache(prompt, fallback_result)
        return jsonify(fallback_result), 200
