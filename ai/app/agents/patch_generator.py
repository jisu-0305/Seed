from .base import BaseAgent
from app.services.openai_client import ask_gpt

class PatchGeneratorAgent(BaseAgent):
    async def run(self, original_code: str, instruction: str) -> str:
        prompt = f"""
            You are PatchGeneratorAgent, an AI specialized in minimally patching source files to fix build errors.

            Inputs:
            - Original file contents:
                {original_code}
            - Error explanation / reason: {instruction}

            Your task:
            - Apply the minimal changes needed to resolve the described error.
            - Preserve every line of the original file that does not require modification—keep all imports, comments, formatting, and indentation exactly as in the input.
            - Keep the existing structure, comments and formatting of the file unchanged as much as possible.
            - Modify only the lines or small blocks necessary to resolve the error described.
            - Return the entire corrected file contents, preserving indentation and line breaks.
            - Return the complete contents of the patched file, with no omissions.
            - Do not wrap your answer in markdown fences or add any commentary—return raw file text.

            Output:
            <the full, patched file contents>

            """
        return await ask_gpt(prompt)
