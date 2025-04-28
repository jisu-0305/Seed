import os
import json
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from app.core.settings import settings
from app.core.redis_conf import redis, REDIS_PREFIX
from app.agents.image_refiner import ImageRefinerAgent

router = APIRouter(prefix="/ai", tags=["AI WebSocket"])

@router.websocket("/ws")
async def websocket_handler(websocket: WebSocket):
    await websocket.accept()
    try:
        while True:
            try:
                data = await websocket.receive_text()
                req = json.loads(data)
                action = req.get("action")

                match action:
                    case "refine":
                        await handle_refine_stream(websocket, req)
                    case _:
                        await websocket.send_json({"error": f"Unknown action: {action}"})

            except Exception as e:
                await websocket.send_json({"error": f"Internal error: {str(e)}"})

    except WebSocketDisconnect:
        print("🔌 WebSocket disconnected")


# 문장 보정
async def handle_refine(websocket: WebSocket, req: dict):
    print("📥 [refine 요청 수신]:", req)
    texts = req.get("texts", [])
    filenames = req.get("filenames", [])

    image_paths = []
    for fname in filenames:
        full_path = os.path.join(settings.UPLOAD_DIR, fname)
        if not os.path.exists(full_path):
            await websocket.send_json({"error": f"파일이 존재하지 않습니다: {fname}"})
            return
        image_paths.append(full_path)

    agent = ImageRefinerAgent()
    result = await agent.run(texts=texts, image_paths=image_paths)
    parsed = json.loads(result)
    await websocket.send_json({"response": parsed})

async def handle_refine_stream(websocket: WebSocket, req: dict):
    print("📥 [refine stream 요청 수신]:", req)
    texts = req.get("texts", [])
    filenames = req.get("filenames", [])

    image_paths = []
    for fname in filenames:
        full_path = os.path.join(settings.UPLOAD_DIR, fname)
        if not os.path.exists(full_path):
            await websocket.send_json({"error": f"파일이 존재하지 않습니다: {fname}"})
            return
        image_paths.append(full_path)

    agent = ImageRefinerAgent()

    # ✅ 스트리밍 응답 전송
    async for chunk in agent.stream(texts=texts, image_paths=image_paths):
        await websocket.send_json({"stream": chunk})

    await websocket.send_json({"done": True})  # ✅ 스트리밍 종료
