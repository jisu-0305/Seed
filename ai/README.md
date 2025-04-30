## FastAPI 프로젝트 시작하기

### 폴더 구조

```
ai/
├── app/                 ← 모든 주요 코드 디렉토리
│   ├── agents/          ← 기능별 ai agent
│   ├── api/             ← 라우터 모음
│   ├── core/            ← config.py, 환경 관련
│   ├── db/              ← crud.py, database.py
│   ├── prompts/              ← 프롬프트
│   ├── services/        ← openai_service.py
│   ├── uploads/         ← 업로드용 로컬 폴더
│   ├── utils/           ← util 함수 모음
│   ├── main.py
├── .env
├── .gitignore
├── README.md
├── requirements.txt
└── run.py               ← 개발 시 실행 함수

```

### 1. 가상환경 세팅

- 가상환경 생성(최초에): `python -m venv seed`

Git Bash에서 가상환경 활성화(윈도우)

```bash
source seed/Scripts/activate
```

### 2. 필요한 패키지 설치

```bash
pip install -r requirements.txt
```

- 설치한 패키지 확인: `pip show openai`

### 3. FastAPI 서버 실행

```bash
python run.py
또는
uvicorn main:app --reload
```

✅ uvicorn main:app

- uvicorn 명령어로 실행하는 건 **ASGI 서버(Uvicorn)**를 명시적으로 사용하는 방식.
- --reload: 파일 수정 시 자동 재시작 (개발용)
- 훨씬 가볍고 빠르게 실행됨. 운영/개발 환경 모두에서 권장되는 방식

💡 결론

- 개발할 땐 → uvicorn main:app --reload
- 배포할 땐 → uvicorn main:app --host 0.0.0.0 --port 8001 같은 식으로 명시 실행

### 4. Swagger 확인

```bash
http://localhost:8001/docs
```

### Redis 서버

vscode에서 터미널 wsl열기

```bash
# 서버 시작
sudo service redis-server start

# 실행 중인지 확인
redis-cli ping

# 종료
sudo service redis-server stop
```

test#1

