from app.core.settings import settings
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

SQLALCHEMY_DATABASE_URL = settings.DB_URL

# DB 엔진 생성
engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)

# 세션 생성기
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 베이스 클래스 (모델이 이걸 상속받음)
Base = declarative_base()
