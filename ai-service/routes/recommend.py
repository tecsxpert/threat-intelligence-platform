from flask import Blueprint, request, jsonify
from services.groq_client import call_groq
from services.cache import get_cache, set_cache
from services.metrics import record_ai_response_time
from datetime import datetime, timezone
import json
import time

recommend_bp = Blueprint("recommend", __name__)

def load_prompt(input_text):
    with open("prompts/recommend.txt", "r") as f:
        template = f.read()
    return template.replace("{input}", input_text)


@recommend_bp.route("/recommend", methods=["POST"])
def recommend():
    data = request.json
    user_input = data.get("input")

    if not user_input:
        return jsonify({"error": "Invalid input"}), 400

    #  input size protection
    if len(user_input) > 2000:
        return jsonify({
            "error": "Input is too long (max 2000 chars)",
            "recommendations": []
        }), 400

    #  FIX: use correct variable
    prompt = load_prompt(user_input)

    #  caching
    cached_result = get_cache(prompt)
    if cached_result:
        cached_result["cached"] = True
        cached_result["generated_at"] = datetime.now(timezone.utc).isoformat()
        return jsonify(cached_result), 200

    #  AI call + metrics
    start_time = time.time()
    ai_response = call_groq(prompt)
    record_ai_response_time(time.time() - start_time)

    if ai_response:
        ai_response = ai_response.strip()
        ai_response = ai_response.replace("```json", "").replace("```", "").strip()

    try:
        recommendations = json.loads(ai_response)

        if not isinstance(recommendations, list):
            raise ValueError("Invalid AI response format")

        result = {
            "recommendations": recommendations[:3],
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": False
        }

        set_cache(prompt, {
            "recommendations": recommendations[:3],
            "is_fallback": False
        })

        return jsonify(result), 200

    except Exception as e:
        print("AI ERROR:", str(e))

        return jsonify({
            "recommendations": [
                {
                    "action_type": "monitor",
                    "description": "Fallback: Monitor system logs",
                    "priority": "high"
                },
                {
                    "action_type": "alert",
                    "description": "Fallback: Notify admin",
                    "priority": "medium"
                },
                {
                    "action_type": "patch",
                    "description": "Fallback: Apply updates",
                    "priority": "low"
                }
            ],
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "is_fallback": True
        }), 200