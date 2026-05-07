import json

from app import app, init_chromadb
from demo_data import DEMO_INPUTS


def response_json(response):
    try:
        return response.get_json() if response.is_json else {"raw": response.get_data(as_text=True)}
    except Exception:
        return {"error": "Unable to read response"}


def call_endpoint(client, path, payload):
    try:
        response = client.post(path, json=payload)
        return {
            "status_code": response.status_code,
            "body": response_json(response)
        }
    except Exception:
        return {
            "status_code": 500,
            "body": {"error": "Request failed"}
        }


def main():
    app.config["RATELIMIT_ENABLED"] = False
    init_chromadb()

    results = []
    with app.test_client() as client:
        for input_text in DEMO_INPUTS:
            describe_output = call_endpoint(client, "/describe", {"input": input_text})
            recommend_output = call_endpoint(client, "/recommend", {"input": input_text})

            recommendations = recommend_output["body"].get("recommendations", [])
            if not recommendations:
                recommendations = recommend_output["body"]

            report_output = call_endpoint(
                client,
                "/generate-report",
                {
                    "threat": input_text,
                    "recommendations": json.dumps(recommendations)
                }
            )

            results.append({
                "input": input_text,
                "describe": describe_output,
                "recommend": recommend_output,
                "report": report_output
            })

    with open("demo_outputs.json", "w", encoding="utf-8") as output_file:
        json.dump(results, output_file, indent=2)

    print("Saved demo_outputs.json")


if __name__ == "__main__":
    main()
