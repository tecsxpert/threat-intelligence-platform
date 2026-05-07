import requests
import os
from dotenv import load_dotenv

load_dotenv()

MODEL_NAME = 'llama-3.1-8b-instant'

# Session for connection pooling - makes requests faster
session = requests.Session()

def call_groq(prompt: str, timeout: int = 10) -> dict:
    """
    Call Groq API with timeout protection for fast responses
    
    Args:
        prompt: The prompt to send to Groq
        timeout: Max seconds to wait (default 10s for <2s average)
    
    Returns:
        dict with 'content' and 'success' keys
    """
    api_key = os.getenv('GROQ_API_KEY')

    if not api_key:
        return {
            'success': False,
            'error': 'GROQ_API_KEY not found in .env file',
            'is_timeout': False
        }

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

        # Use session for faster subsequent requests
        response = session.post(url, headers=headers, json=data, timeout=timeout)

        if response.status_code != 200:
            return {
                'success': False,
                'error': f'Groq API error: {response.text}',
                'is_timeout': False
            }

        result = response.json()
        return {
            'success': True,
            'content': result['choices'][0]['message']['content'],
            'is_timeout': False
        }

    except requests.Timeout:
        return {
            'success': False,
            'error': f'API call timed out after {timeout} seconds',
            'is_timeout': True
        }
    except Exception as e:
        return {
            'success': False,
            'error': f'Error: {str(e)}',
            'is_timeout': False
        }
