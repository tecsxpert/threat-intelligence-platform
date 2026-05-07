import requests
import json

BASE_URL = "http://127.0.0.1:5000"

print("\nProblem: Organizations need quick and reliable threat analysis\n")

print("Architecture:")
print("- Flask API")
print("- Groq API")
print("- Redis (optional)")
print("- Fallback system\n")

data = {
    "input": "Unauthorized access detected in admin panel"
}

try:
    print("Calling /describe...")
    res1 = requests.post(f"{BASE_URL}/describe", json=data)
    print(json.dumps(res1.json(), indent=2))

    print("\nCalling /recommend...")
    res2 = requests.post(f"{BASE_URL}/recommend", json=data)
    print(json.dumps(res2.json(), indent=2))

    print("\nCalling /generate-report...")
    report_data = {
        "threat": data["input"],
        "recommendations": "Enable MFA and restrict access"
    }
    res3 = requests.post(f"{BASE_URL}/generate-report", json=report_data)
    print(json.dumps(res3.json(), indent=2))

except Exception as e:
    print("Error:", e)