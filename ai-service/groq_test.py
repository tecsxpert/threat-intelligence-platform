import json
import os

import requests
from dotenv import load_dotenv


load_dotenv()

PORT = os.getenv("PORT", "5000")
BASE_URL = f"http://127.0.0.1:{PORT}"
TEST_INPUT = "SQL injection detected"


def check_api_key():
    api_key = os.getenv("GROQ_API_KEY")
    if not api_key:
        print("API key not found")
        return False

    print("API key found")
    return True


def get_json_response(response):
    try:
        return response.json()
    except Exception:
        return {}


def test_endpoint(endpoint, payload):
    print(f"Testing {endpoint}...")

    try:
        response = requests.post(f"{BASE_URL}{endpoint}", json=payload, timeout=30)
        body = get_json_response(response)
        is_fallback = body.get("is_fallback")

        success = response.status_code == 200 and is_fallback is False
        message = "Groq working" if success else "Fallback used"
        status = "success" if success else "failure"

        print(f"Endpoint: {endpoint}")
        print(f"Status: {status}")
        print(f"is_fallback: {is_fallback}")
        print(f"Message: {message}")
        print("")

        return {
            "endpoint": endpoint,
            "status": status,
            "is_fallback": is_fallback,
            "message": message
        }

    except Exception as error:
        print(f"Endpoint: {endpoint}")
        print("Status: failure")
        print("is_fallback: unknown")
        print(f"Message: request failed: {error}")
        print("")

        return {
            "endpoint": endpoint,
            "status": "failure",
            "is_fallback": None,
            "message": "Request failed"
        }


def main():
    check_api_key()

    results = []
    results.append(test_endpoint("/describe", {"input": TEST_INPUT}))
    results.append(test_endpoint("/recommend", {"input": TEST_INPUT}))
    results.append(test_endpoint(
        "/generate-report",
        {
            "threat": TEST_INPUT,
            "recommendations": "Use parameterized queries and validate user input"
        }
    ))

    with open("groq_test_results.json", "w", encoding="utf-8") as results_file:
        json.dump(results, results_file, indent=2)

    print("Saved groq_test_results.json")


if __name__ == "__main__":
    main()
