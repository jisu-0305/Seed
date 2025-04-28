from redis.asyncio import Redis
from app.core.settings import settings

redis = Redis(
    host=settings.REDIS_HOST,
    port=settings.REDIS_PORT,
    db=settings.REDIS_DB,
    decode_responses=True
)

REDIS_PREFIX = settings.REDIS_PREFIX