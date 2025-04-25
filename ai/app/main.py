import os
from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
from fastapi.routing import APIRoute
from starlette.routing import WebSocketRoute
from app.core.settings import settings
from app.api.ai import router as ai_router
from app.api.ai_ws import router as ai_ws_router

app = FastAPI()

UPLOADS_DIR = os.path.join(os.path.dirname(__file__), "uploads")
app.mount("/static/uploads", StaticFiles(directory=UPLOADS_DIR), name="uploads")

app.include_router(ai_router)
app.include_router(ai_ws_router)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.DOMAIN_URL, settings.LOCAL_URL, settings.WS_URL],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
async def show_routes():
    print("\n📌 Registered routes:")
    for route in app.routes:
        if isinstance(route, APIRoute):
            print(f"🟢 REST  {route.path} → {route.name}")
        elif isinstance(route, WebSocketRoute):
            print(f"🟡 WS    {route.path} → {route.name}")

