# AI Service

Python Flask AI service for the Threat Intelligence Platform.

It provides endpoints to describe threats, recommend security actions, generate reports, and check service health. The service uses the Groq API for AI responses, Redis for caching when available, and safe fallback responses when external services fail.

## Setup

Create and activate a virtual environment:

```bash
python -m venv .venv
```

Windows:

```powershell
.\.venv\Scripts\activate
```

Install dependencies:

```bash
pip install -r requirements.txt
```

## Environment Variables

Create a `.env` file in this folder. Use `.env.example` as a template:

```env
GROQ_API_KEY=your_key_here
PORT=5000
REDIS_HOST=localhost
REDIS_PORT=6379
```

Do not commit real API keys.

## Run Locally

```bash
python app.py
```

The service runs on:

```text
http://localhost:5000
```

## Run With Docker

Build the image:

```bash
docker build -t ai-service .
```

Run the container:

```bash
docker run -p 5000:5000 --env-file .env ai-service
```

## API Endpoints

### GET /health

Checks whether the service is running.

Example:

```bash
curl http://localhost:5000/health
```

Example response:

```json
{
  "status": "ok",
  "model": "llama-3.1-8b-instant",
  "avg_response_time_ms": 1200.5,
  "uptime_seconds": 30.2
}
```

### POST /describe

Describes a security threat.

Example request:

```bash
curl -X POST http://localhost:5000/describe \
  -H "Content-Type: application/json" \
  -d "{\"input\":\"SQL injection detected in login form\"}"
```

Example response:

```json
{
  "generated_at": "2026-05-06T10:30:45Z",
  "data": {
    "description": "SQL injection can allow attackers to read or modify database records.",
    "severity": "high"
  },
  "is_fallback": false
}
```

### POST /recommend

Returns recommended security actions.

Example request:

```bash
curl -X POST http://localhost:5000/recommend \
  -H "Content-Type: application/json" \
  -d "{\"input\":\"Open database port exposed to internet\"}"
```

Example response:

```json
{
  "recommendations": [
    {
      "action_type": "restrict",
      "description": "Close the public port or restrict access using firewall rules.",
      "priority": "high"
    }
  ],
  "generated_at": "2026-05-06T10:30:45Z",
  "is_fallback": false
}
```

### POST /generate-report

Generates a report from a threat and recommendations.

Example request:

```bash
curl -X POST http://localhost:5000/generate-report \
  -H "Content-Type: application/json" \
  -d "{\"threat\":\"SQL injection detected\",\"recommendations\":\"Use parameterized queries and validate input\"}"
```

Example response:

```json
{
  "report": {
    "title": "SQL Injection Security Report",
    "summary": "The application may be vulnerable to database query manipulation.",
    "recommendations": [
      "Use parameterized queries",
      "Validate and sanitize user input"
    ]
  },
  "generated_at": "2026-05-06T10:30:45Z",
  "is_fallback": false
}
```

## Fallback Behavior

If Groq, Redis, ChromaDB, or optional embedding packages are unavailable, the service continues running and returns safe fallback responses where needed.

## Project Files

- `app.py` - Flask app entry point
- `routes/` - API route files
- `services/` - Groq, cache, metrics, and fallback helpers
- `requirements.txt` - Python dependencies
- `Dockerfile` - Docker packaging
- `.env.example` - environment variable template
