import json
from .base import BaseAgent
import base64
from app.services.openai_client import ask_gpt_with_image
from app.services.openai_client import ask_gpt_with_image_stream

class ImageRefinerAgent(BaseAgent):
    async def run(self, texts: list[str], image_paths: list[str]):
        images_base64 = []
        for path in image_paths:
            with open(path, "rb") as f:
                image_bytes = f.read()
            base64_image = base64.b64encode(image_bytes).decode("utf-8")
            images_base64.append(base64_image)
        
        prompt = f"""
        다음은 편지에 사용된 문장들입니다.

        각 문장은 사진들과 함께 쓰이지만, 사진 속 장면이 충분히 반영되지 않았습니다.  
        각 문장을 감정과 분위기를 살려 감성적으로 보완해 주세요.

        ※ 출력은 **JSON 리스트만** 응답하세요.
        ※ 각 항목은 반드시 **쌍따옴표(")로 감싼 문자열**이어야 하며, 전체는 리스트 형태여야 합니다.  
        - ✅ **백틱( ```)이나 "json" 같은 포맷 마크다운을 절대 포함하지 마세요.**

        ---

        원본 문장 목록:
        {json.dumps(texts, ensure_ascii=False)}
        """
        return await ask_gpt_with_image(prompt, images_base64)
    
    async def stream(self, texts: list[str], image_paths: list[str]):
        images_base64 = []
        for path in image_paths:
            with open(path, "rb") as f:
                image_bytes = f.read()
            base64_image = base64.b64encode(image_bytes).decode("utf-8")
            images_base64.append(base64_image)

        prompt = f"""
        다음은 편지에 사용된 문장들입니다.

        각 문장은 사진들과 함께 쓰이지만, 사진 속 장면이 충분히 반영되지 않았습니다.  
        각 문장을 감정과 분위기를 살려 감성적으로 보완해 주세요.

        - 각 문장은 줄바꿈으로 구분해서 출력해 주세요.  
        - JSON 형식은 사용하지 마세요.  
        - 마크다운, 백틱 없이 평문만 출력해 주세요.

        ---

        원본 문장 목록:
        {json.dumps(texts, ensure_ascii=False)}
        """

        async for chunk in ask_gpt_with_image_stream(prompt, images_base64):
            yield chunk