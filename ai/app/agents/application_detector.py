from .base import BaseAgent
from app.services.openai_client import ask_gpt

import json
from typing import List

class AppInferenceAgent(BaseAgent):
    async def run(
        self,
        gitDiff: List[dict],
        jenkinsLog: str,
        applicationNames: List[str]
    ) -> str:
        prompt = f"""
            You are AppInferenceAgent, an AI specialized in inferring which application(s) are causing a failure based on diff, build logs, and known application names.

            INPUT (as JSON):
            {{
            "gitDiff": {json.dumps(gitDiff, ensure_ascii=False)},
            "jenkinsLog": "{jenkinsLog}",
            "applicationNames": {applicationNames}
            }}

            TASKS:
            1. 분석된 gitDiff와 jenkinsLog를 바탕으로, 실패와 연관성이 높은 애플리케이션 이름을 고르세요.
            2. applicationNames 리스트에서만 고르며, 없는 이름은 포함하지 마세요.
            3. 간단한 한 줄 요약(“Reason”)과 함께, “suspectedApps” 필드에 이름 배열로 반환하세요.

            OUTPUT (JSON):
            {{
            "Reason": "<한 줄 요약>",
            "suspectedApps": ["<app1>", "<app2>", ...]
            }}

            Requirements:
            - 반드시 하나의 JSON 오브젝트만 반환합니다.
            - 다른 필드는 허용되지 않습니다.
            - 마크다운이나 코드펜스 없이 순수 JSON만 출력하세요.
            """
        return await ask_gpt(prompt)