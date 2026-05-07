import json
import time

import requests


BASE_URL = "http://127.0.0.1:5000"
TIME_LIMIT_MS = 2000
TEST_INPUT = "SQL injection detected"


def print_result(endpoint, response_time, success, fallback_used=False):
    status = "success" if success else "fail"
    print(f"{endpoint} | {response_time} ms | {status}")
    if response_time > TIME_LIMIT_MS:
        print(f"WARNING: {endpoint} took more than {TIME_LIMIT_MS} ms")
    if fallback_used:
        print(f"{endpoint} used fallback response")


def call_endpoint(endpoint, method="GET", payload=None, input_text=""):
    start_time = time.time()

    try:
        if method == "POST":
            response = requests.post(f"{BASE_URL}{endpoint}", json=payload, timeout=30)
        else:
            response = requests.get(f"{BASE_URL}{endpoint}", timeout=30)

        response_time = round((time.time() - start_time) * 1000, 2)
        success = response.status_code < 400

        try:
            body = response.json()
        except Exception:
            body = {}

        cache_used = body.get("cached") is True
        fallback_used = body.get("is_fallback") is True
        print_result(endpoint, response_time, success, fallback_used)

        return {
            "endpoint": endpoint,
            "input": input_text,
            "response_time": response_time,
            "cache_used": cache_used,
            "fallback_used": fallback_used,
            "status": "success" if success else "fail",
            "status_code": response.status_code
        }

    except Exception:
        response_time = round((time.time() - start_time) * 1000, 2)
        print_result(endpoint, response_time, False)
        return {
            "endpoint": endpoint,
            "input": input_text,
            "response_time": response_time,
            "cache_used": False,
            "fallback_used": False,
            "status": "fail",
            "status_code": None
        }


def check_cache(endpoint, payload):
    print(f"Checking cache for {endpoint}...")
    first = call_endpoint(endpoint, "POST", payload, payload.get("input", ""))
    second = call_endpoint(endpoint, "POST", payload, payload.get("input", ""))

    cache_working = second["cache_used"] or second["response_time"] < first["response_time"]
    if cache_working:
        print("Cache working")
    else:
        print("Cache not faster or Redis cache unavailable")

    second["cache_used"] = cache_working
    return first, second


def main():
    results = []

    print("Testing endpoints...")
    results.append(call_endpoint("/health"))

    describe_payload = {"input": TEST_INPUT}
    recommend_payload = {"input": TEST_INPUT}
    report_payload = {
        "threat": TEST_INPUT,
        "recommendations": "Use parameterized queries and validate input"
    }

    results.append(call_endpoint("/describe", "POST", describe_payload, TEST_INPUT))
    results.append(call_endpoint("/recommend", "POST", recommend_payload, TEST_INPUT))
    results.append(call_endpoint("/generate-report", "POST", report_payload, TEST_INPUT))

    print("Testing cache...")
    first_cache_test, second_cache_test = check_cache("/describe", describe_payload)
    results.append(first_cache_test)
    results.append(second_cache_test)

    print("Testing fallback...")
    fallback_result = call_endpoint(
        "/describe",
        "POST",
        {"input": "Fallback test for disabled Groq"},
        "Fallback test for disabled Groq"
    )
    if fallback_result["fallback_used"]:
        print("Fallback working")
    else:
        print("Fallback not detected. Run app with GROQ_API_KEY empty to force fallback.")
    results.append(fallback_result)

    with open("performance_results.json", "w", encoding="utf-8") as results_file:
        json.dump(results, results_file, indent=2)

    print("Saved performance_results.json")


if __name__ == "__main__":
    main()
