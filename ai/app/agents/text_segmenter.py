from .base import BaseAgent
from app.services.openai_client import ask_gpt

class TextSegmenterAgent(BaseAgent):
    async def run(self, text: str, count: int = 5):
        prompt = f"""
        아래는 한 사람이 쓴 편지입니다. 이 편지를 자연스러운 문맥 흐름에 따라 총 {count}개의 단락으로 나눠주세요.

        - 같은 주제나 분위기를 공유하는 문장들을 하나의 단락으로 묶어야 합니다.
        - 문장은 절대 수정하지 마세요. 입력된 문장을 그대로 사용하세요.
        - 단락만 나누고, 내용을 바꾸거나 추가하지 마세요.
        - 반드시 {count}개의 단락으로 나누세요.
        - 출력은 JSON 리스트 형식으로만 해주세요. 예시는 다음과 같습니다:

        [
        "단락1",
        "단락2",
        "단락3"
        ]

        다음은 편지 원문입니다:
        ---
        {text}
        """
        return await ask_gpt(prompt)
