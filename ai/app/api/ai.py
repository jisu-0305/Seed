import os
import json
from app.core.settings import settings

from typing import List
from fastapi import APIRouter, HTTPException, Form, Body
from fastapi.responses import JSONResponse, PlainTextResponse
from typing import List
from pydantic import BaseModel, ValidationError

from app.agents.file_locator import FileLocatorAgent
from app.agents.error_resolver import BuildErrorResolverAgent
from app.agents.patch_generator import PatchGeneratorAgent
from app.agents.error_reporter import ErrorReportAgent

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
    • diff: 구조화된 JSON 객체 (commit, diffs[])  
    • tree: 프로젝트 폴더 구조  
    • log: 어플리케이션 에러 로그  
    
    FileLocatorAgent를 호출하여, 현재 에러와 관련있는 파일 목록을 반환합니다.
    
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
        # print("💀💫 AI 응답: ", result)
        parsed = json.loads(result)
        return {"response": parsed}
    except json.JSONDecodeError as je:
        raise HTTPException(500, f"AI 응답 JSON 파싱 실패: {je}")
    except Exception as e:
        raise HTTPException(500, str(e))

# ─────────────────────────────────────────────────────────────────────────────
# 2) Error Resolver
# ─────────────────────────────────────────────────────────────────────────────

class FilePayload(BaseModel):
    path: str
    code: str

class ResolvePayload(BaseModel):
    errorSummary: str
    cause: str
    resolutionHint: str
    files: List[FilePayload]
    
@router.post("/resolve", summary="Resolve build/runtime errors")
async def resolve_error(
    errorSummary: str = Form(..., description="간단한 에러 요약"),
    cause: str = Form(..., description="에러 원인 설명"),
    resolutionHint: str = Form(..., description="해결 힌트"),
    files_raw: str = Form(..., description="수정 대상 파일 리스트 (JSON 문자열)")
):
    """
    • errorSummary: 에러 요약 문자열  
    • cause: 원인 설명 문자열  
    • resolutionHint: 해결 힌트 문자열  
    • files_raw: JSON 문자열, [{\"path\": \"...\", \"code\": \"...\"}, ...]  
    
    BuildErrorResolverAgent를 호출하여, 각 파일의 수정 지시사항 및 요약 보고서를 반환합니다.
    
    """
    # 1) files_raw 파싱
    try:
        files_list = json.loads(files_raw)
    except json.JSONDecodeError as e:
        raise HTTPException(status_code=400, detail=f"files JSON 파싱 실패: {e}")

    # 2) Pydantic 모델 검증
    try:
        payload = ResolvePayload(
            errorSummary=errorSummary,
            cause=cause,
            resolutionHint=resolutionHint,
            files=files_list
        )
    except ValidationError as ve:
        raise HTTPException(status_code=422, detail=ve.errors())

    # 3) Agent 호출
    try:
        agent = BuildErrorResolverAgent()
        ai_response = await agent.run(
            errorSummary=payload.errorSummary,
            cause=payload.cause,
            resolutionHint=payload.resolutionHint,
            files=[f.dict() for f in payload.files]
        )
        parsed = json.loads(ai_response)
        return {"response": parsed}
    except json.JSONDecodeError as je:
        raise HTTPException(status_code=500, detail=f"AI 응답 JSON 파싱 실패: {je}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# ─────────────────────────────────────────────────────────────────────────────
# 3) Patch Generator
# ─────────────────────────────────────────────────────────────────────────────

class PatchResponse(BaseModel):
    patched_code: str

@router.post("/patch", response_model=PatchResponse)
async def generate_patch_text(
    original_code: str = Form(..., description="전체 파일 코드"),
    instruction: str = Form(..., description="에러 수정 지시 사항")
):
    """
    • original_code: 원본 전체 파일 콘텐츠  
    • instruction: 에러 수정 지시 사항  

    PatchGeneratorAgent를 호출하여, 최소한의 수정만 가해진 전체 파일 코드를 반환합니다.
    """
    try:
        agent = PatchGeneratorAgent()
        patched = await agent.run(
            original_code=original_code,
            instruction=instruction
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI 응답 실패: {e}")
    
    return PatchResponse(patched_code=patched)
    
@router.post("/patch/file")
async def generate_patch_file(
    path: str = Form(..., description="수정할 파일의 리포지토리 내 경로 (예: src/.../Foo.java)"),
    original_code: str = Form(..., description="전체 파일 코드"),
    instruction: str = Form(..., description="에러 수정 지시 사항")
):
    """
    • path: 원본 파일 경로 (확장자 추출용)  
    • original_code: 원본 전체 파일 콘텐츠  
    • instruction: 에러 수정 지시 사항 

    PatchGeneratorAgent를 호출하여, 최소한의 수정만 가해진 전체 파일 코드를 반환합니다.
    """
    # 1) 패치 생성
    try:
        agent = PatchGeneratorAgent()
        patched = await agent.run(
            original_code=original_code,
            instruction=instruction
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"AI 응답 실패: {e}")

    # 2) 파일명 추출
    filename = os.path.basename(path)

    # 3) 응답: 텍스트 본문 + 다운로드 헤더
    headers = {"Content-Disposition": f'attachment; filename="{filename}"'}
    return PlainTextResponse(content=patched, media_type="text/plain", headers=headers)

# ─────────────────────────────────────────────────────────────────────────────
# 4) Error Reporter
# ─────────────────────────────────────────────────────────────────────────────

class FileFix(BaseModel):
    path: str
    instruction: str
    explanation: str

class ResolutionReport(BaseModel):
    errorSummary: str
    cause: str
    finalResolution: str

class ErrorReportRequest(BaseModel):
    fileFixes: List[FileFix]
    resolutionReport: ResolutionReport
    
@router.post("/report", summary="Generate human-readable error resolution report")
async def generate_error_report(request: ErrorReportRequest):
    """
    요청 예시 (JSON body):
    {
      "fileFixes": [
        {
          "path": "...",
          "instruction": "...",
          "explanation": "..."
        }
      ],
      "resolutionReport": {
        "errorSummary": "...",
        "cause": "...",
        "finalResolution": "..."
      }
    }
    
    ErrorReportAgent를 호출하여, 해결한 빌드 에러에 대한 보고서를 작성합니다.
    """
    # 1) Pydantic 검증된 객체를 JSON 문자열로 직렬화
    try:
        payload_json = json.dumps(request.dict(), ensure_ascii=False)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Payload JSON serialization failed: {e}")

    # 2) Agent 호출
    try:
        agent = ErrorReportAgent()
        ai_response = await agent.run(payload_json)
        return json.loads(ai_response)
    except json.JSONDecodeError as je:
        raise HTTPException(status_code=500, detail=f"AI response JSON parsing failed: {je}")
    except ValidationError as ve:
        raise HTTPException(status_code=422, detail=ve.errors())
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))