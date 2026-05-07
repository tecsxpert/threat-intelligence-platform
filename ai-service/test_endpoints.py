import json
import time

import requests


BASE_URL = "http://127.0.0.1:5000"
TEST_INPUTS = [
    "SQL injection detected",
    "XSS attack detected",
    "Phishing email reported",
    "Open port exposed",
    "Weak password found",
    "Ransomware activity detected"
]


def call_api(endpoint, method="GET", payload=None, input_text=""):
    url = f"{BASE_URL}{endpoint}"
    print(f"Calling {endpoint}...")

    start_time = time.time()
    try:
        if method == "POST":
            response = requests.post(url, json=payload, timeout=30)
        else:
            response = requests.get(url, timeout=30)

        response_time = round((time.time() - start_time) * 1000, 2)
        status = "success" if response.status_code < 400 else "failed"

        print(f"{endpoint} | {response_time} ms | {status}")
        return {
            "endpoint": endpoint,
            "input": input_text,
            "response_time": response_time,
            "status": status,
            "status_code": response.status_code
        }, response

    except Exception:
        response_time = round((time.time() - start_time) * 1000, 2)
        print(f"{endpoint} | {response_time} ms | failed")
        return {
            "endpoint": endpoint,
            "input": input_text,
            "response_time": response_time,
            "status": "failed",
            "status_code": None
        }, None


def main():
    results = []

    health_result, _ = call_api("/health")
    results.append(health_result)

    for input_text in TEST_INPUTS:
        describe_result, describe_response = call_api(
            "/describe",
            method="POST",
            payload={"input": input_text},
            input_text=input_text
        )
        results.append(describe_result)

        recommend_result, recommend_response = call_api(
            "/recommend",
            method="POST",
            payload={"input": input_text},
            input_text=input_text
        )
        results.append(recommend_result)

        recommendations = "No recommendations available"
        try:
            recommend_body = recommend_response.json() if recommend_response else {}
            recommendations = json.dumps(recommend_body.get("recommendations", recommendations))
        except Exception:
            pass

        report_result, _ = call_api(
            "/generate-report",
            method="POST",
            payload={
                "threat": input_text,
                "recommendations": recommendations
            },
            input_text=input_text
        )
        results.append(report_result)

    with open("test_results.json", "w", encoding="utf-8") as results_file:
        json.dump(results, results_file, indent=2)

    print("Saved test_results.json")


if __name__ == "__main__":
    main()
