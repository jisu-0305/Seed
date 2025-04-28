from .base import BaseAgent
from app.services.openai_client import ask_gpt

class PatchGeneratorAgent(BaseAgent):
    async def run(self, original_code: str, error_reason: str) -> str:
        prompt = f"""
            You are PatchGeneratorAgent, an AI specialized in minimally patching source files to fix build errors.

            Inputs:
            - Original file contents:
                {original_code}
            - Error explanation / reason: {error_reason}

            Your task:
            - Keep the existing structure, comments and formatting of the file unchanged as much as possible.
            - Modify only the lines or small blocks necessary to resolve the error described.
            - Return the entire corrected file contents, preserving indentation and line breaks.
            - Do not wrap your answer in markdown fences or add any commentary—return raw file text.

            Output:
            <the full, patched file contents>

            """
        return await ask_gpt(prompt)
