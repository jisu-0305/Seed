from .base import BaseAgent
from app.services.openai_client import ask_gpt

class ErrorReportAgent(BaseAgent):
    async def run(self, payload_json: str) -> str:
        prompt = f"""
            당신은 ErrorResolutionReportAgent입니다. 해결된 빌드/런타임 에러에 대한 최종 보고서를 생성하는 전문가 역할을 수행하세요.

            입력(JSON):
            {payload_json}

            작업:
            1. "title" 필드에는 간단한 한글 제목을 작성하세요.
            2. “summary” 필드에는 에러의 배경과 처리 과정을 아우르는 상세한 한글 문단(3~5문장)으로 작성하세요.
            3. “appliedFiles” 필드에는 fileFixes 배열에서 각 path의 파일명을 추출해 영어 배열 형태로 나열하세요. (경로 제외, 파일명만)
            4. “additionalNotes” 필드에는 fileFixes의 explanation이나, 보고서에 포함할 기타 유의사항을 자유롭게 기술하세요.  
                만약 추가로 언급할 사항이 없다면 빈 문자열("")로 설정합니다.

            출력 형식(JSON):
            {{
                "title": "<보고서의 제목>",
                "summary": "<에러해결에 대한 상세 설명>",
                "appliedFiles": ["BasicServiceImpl.java", "SecondaryServiceImpl.java"],
                "additionalNotes": "<추가로 전달할 설명>"
            }}

            """
        return await ask_gpt(prompt)
