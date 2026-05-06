import requests
import os
from dotenv import load_dotenv

load_dotenv()

MODEL_NAME = 'llama-3.1-8b-instant'

def call_groq(prompt: str) -> str:
    api_key = os.getenv('GROQ_API_KEY')

    if not api_key:
        raise Exception('GROQ_API_KEY not found in .env file')

    try:
        url = 'https://api.groq.com/openai/v1/chat/completions'

        headers = {
            'Authorization': f'Bearer {api_key}',
            'Content-Type': 'application/json'
        }

        data = {
            'model': MODEL_NAME,
            'messages': [
                {'role': 'user', 'content': prompt}
            ],
            'temperature': 0.3
        }

        response = requests.post(url, headers=headers, json=data, timeout=30)

        if response.status_code != 200:
            return f'Groq API error: {response.text}'

        result = response.json()
        return result['choices'][0]['message']['content']

    except Exception as e:
        return f'Error: {str(e)}'