from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    API_HOST: str
    API_PORT: int
    REDIS_HOST: str
    REDIS_PORT: int
    REDIS_DB: int
    REDIS_PREFIX: str
    DB_URL: str
    UPLOAD_DIR: str
    OPENAI_API_KEY: str
    DOMAIN_URL: str
    LOCAL_URL: str
    WS_URL: str

    class Config:
        env_file = ".env"

settings = Settings()
