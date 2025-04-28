from .base import BaseAgent
from app.services.openai_client import ask_gpt

class FileLocatorAgent(BaseAgent):
    async def run(self, diff: str, tree: str, log: str):
        prompt = f"""
            You are FileLocatorAgent, an AI specialized in pinpointing which source files must change to fix build errors.

            Given the following three inputs:
            1. Git diff (JSON):
            {diff}

            2. Project tree (directory structure):
            {tree}

            3. Build or runtime error log:
            {log}

           Task:
            - Identify each file that directly contributes to the build failure.
            - Return only their paths in a JSON array.

            Output format:
            Return one JSON array named "suspectFiles". Each element is an object with exactly one key:

            [
            {{
                "path": "<repo-relative file path>"
            }},
            ...
            ]

            Requirements:
            - Output must be **one JSON array**.
            - Do not include any other fields.
            - Do **not** wrap output in markdown, code fences, or extra commentary.
            - Only include files that actually participate in the failure.
            
            Example:
            [
            {{
                "path": "backend/src/main/java/org/example/backend/domain/userNumber/service/BasicServiceImpl.java"
            }},
            {{
                "path": "backend/src/main/java/org/example/backend/domain/userNumber/service/SecondaryServiceImpl.java"
            }}
            ]
        """
        return await ask_gpt(prompt)
