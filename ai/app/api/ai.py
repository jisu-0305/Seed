import os
import json
from app.core.settings import settings

from typing import List
from fastapi import APIRouter, HTTPException, Form, Body
from fastapi.responses import JSONResponse, PlainTextResponse
from typing import List, Dict
from pydantic import BaseModel, ValidationError

from app.agents.image_refiner import ImageRefinerAgent
from app.agents.file_locator import FileLocatorAgent
from ai.app.agents.patch_generator import PatchGeneratorAgent
from app.agents.multi_patch_agent import MultiPatchAgent

router = APIRouter(prefix="/ai", tags=["AI Agents"])

# ─────────────────────────────────────────────────────────────────────────────
# 1) File Locator
# ─────────────────────────────────────────────────────────────────────────────

class CommitInfo(BaseModel):
    title: str
    message: str

class DiffHunk(BaseModel):
    diff: str
    new_path: str
    old_path: str
    a_mode: str
    b_mode: str
    new_file: bool
    renamed_file: bool
    deleted_file: bool
    generated_file: bool | None

class DiffPayload(BaseModel):
    commit: CommitInfo
    diffs: List[DiffHunk]

@router.post("/filepath")
async def file_locator(
    diff_raw: str = Form(..., description="구조화된 JSON 객체 (commit, diffs[])"),
    tree: str = Form(..., description="프로젝트 폴더 구조"),
    log: str = Form(..., description="어플리케이션 에러 로그")
):
    """
    - diff: 구조화된 JSON 객체 (commit, diffs[])
    - tree: 프로젝트 폴더 구조
    - log: 어플리케이션 에러 로그
    """
     # 1) diff_raw를 JSON으로 파싱
    try:
        diff_dict = json.loads(diff_raw)
    except json.JSONDecodeError as e:
        raise HTTPException(status_code=400, detail=f"diff JSON 파싱 실패: {e}")

    # 2) Pydantic 모델로 검증
    try:
        diff_payload = DiffPayload(**diff_dict)
    except ValidationError as ve:
        raise HTTPException(status_code=422, detail=ve.errors())
    
    try:
        agent = FileLocatorAgent()
        result = await agent.run(
            diff=diff_payload.dict(),
            tree=tree,
            log=log
        )
        print("💀💫 AI 응답: ", result)
        parsed = json.loads(result)
        return {"suspectFiles": parsed}
    except json.JSONDecodeError as je:
        raise HTTPException(500, f"AI 응답 JSON 파싱 실패: {je}")
    except Exception as e:
        raise HTTPException(500, str(e))


# ─────────────────────────────────────────────────────────────────────────────
# 2) Image Refiner
# ─────────────────────────────────────────────────────────────────────────────

class PatchResponse(BaseModel):
    patched_code: str

@router.post("/patch", response_model=PatchResponse)
async def generate_code(
    path: str = Form(..., description="수정할 파일의 리포지토리 내 경로 (예: src/.../Foo.java)"),
    original_code: str = Form(..., description="문제 전의 전체 파일 코드 (멀티라인)"),
    reason: str = Form(..., description="FileLocatorAgent가 알려준 에러 이유")
):
    """
    • path: 원본 파일 경로 (확장자 추출용)  
    • original_code: 원본 전체 파일 콘텐츠 (멀티라인 textarea)  
    • reason: 에러 요약 설명 (textarea)  

    PatchGeneratorAgent를 호출하여, 최소한의 수정만 가해진
    전체 파일 코드를 반환합니다.
    """
    try:
        agent = PatchGeneratorAgent()
        patched = await agent.run(
            original_code=original_code,
            error_reason=reason
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI 응답 실패: {e}")
    
    return PatchResponse(patched_code=patched)
    
@router.post("/patch-file")
async def download_patch(
    path: str = Form(..., description="수정할 파일의 리포지토리 내 경로 (예: src/.../Foo.java)"),
    original_code: str = Form(..., description="문제 전의 전체 파일 코드 (멀티라인)"),
    reason: str = Form(..., description="FileLocatorAgent가 알려준 에러 이유")
):
    """
    • path: 원본 파일 경로 (확장자 추출용)  
    • original_code: 원본 전체 파일 콘텐츠 (멀티라인 textarea)  
    • reason: 에러 요약 설명 (textarea)  

    PatchGeneratorAgent를 호출하여, 최소한의 수정만 가해진
    전체 파일 코드를 반환합니다.
    """
    # 1) 패치 생성
    try:
        agent = PatchGeneratorAgent()
        patched = await agent.run(
            original_code=original_code,
            error_reason=reason
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI 응답 실패: {e}")

    # 2) 파일명 추출
    filename = os.path.basename(path)

    # 3) 응답: 텍스트 본문 + 다운로드 헤더
    headers = {"Content-Disposition": f'attachment; filename="{filename}"'}
    return PlainTextResponse(content=patched, media_type="text/plain", headers=headers)





# ─────────────────────────────────────────────────────────────────────────────
# 기타) Image Refiner
# ─────────────────────────────────────────────────────────────────────────────

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

