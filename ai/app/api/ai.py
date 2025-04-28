import os
import json
from app.core.settings import settings
from typing import List
from fastapi import APIRouter, UploadFile, File, Form
from fastapi.responses import JSONResponse

from app.agents.image_refiner import ImageRefinerAgent
from app.agents.text_segmenter import TextSegmenterAgent

router = APIRouter(prefix="/ai", tags=["AI Agents"])

@router.post("/refine")
async def refine_with_image(
    texts: List[str] = Form(...),
    filenames: List[str] = Form(...)
):
    """
    업로드된 이미지 및 글 기반 문장 보정
    """
    try:
        image_paths = []
        for fname in filenames:
            full_path = os.path.normpath(os.path.join(settings.UPLOAD_DIR, fname))
            print("📁 실제 찾는 이미지 경로:", full_path)
            print("📂 파일 존재?", os.path.exists(full_path))
            if not os.path.exists(full_path):
                return JSONResponse(status_code=404, content={"error": f"{fname} not found"})
            image_paths.append(full_path)

        agent = ImageRefinerAgent()
        result = await agent.run(texts=texts, image_paths=image_paths)
        print("💌 생성된 풍부한 문장:", result)
        
        parsed = json.loads(result)
        if not isinstance(parsed, list):
            raise ValueError("응답이 리스트 형식이 아님")
        return {"response": parsed}
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": f"AI 응답 파싱 실패: {str(e)}"})

@router.post("/segment")
async def segment_text(text: str = Form(...), count: int = Form(5)):
    """
    긴 글을 문맥별로 N묶음으로 나누는 기능
    """
    try:
        agent = TextSegmenterAgent()
        result = await agent.run(text=text, count=count)
        parsed_result = json.loads(result)
        print("💌 생성된 글조각:", parsed_result)
        return {"response": parsed_result}
    except json.JSONDecodeError as je:
        return JSONResponse(status_code=500, content={"error": f"AI 응답을 JSON으로 파싱할 수 없습니다: {str(je)}"})
    except Exception as e:
        return JSONResponse(status_code=500, content={"error": str(e)})
