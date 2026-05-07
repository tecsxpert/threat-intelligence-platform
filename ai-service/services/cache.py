import redis
import hashlib
import json
import os

# Connect to Redis
redis_client = redis.Redis(
    host=os.getenv("REDIS_HOST", "localhost"),
    port=int(os.getenv("REDIS_PORT", 6379)),
    db=0,
    decode_responses=True,
    socket_connect_timeout=0.2,
    socket_timeout=0.2
)

TTL_SECONDS = 900  # 15 minutes

def generate_key(prompt):
    return hashlib.sha256(prompt.encode()).hexdigest()

def get_cache(prompt):
    key = generate_key(prompt)
    data = redis_client.get(key)
    if data:
        return json.loads(data)
    return None

def set_cache(prompt, value):
    key = generate_key(prompt)
    redis_client.setex(key, TTL_SECONDS, json.dumps(value))
