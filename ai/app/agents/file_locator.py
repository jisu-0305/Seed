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

           Tasks:
            - Identify each file that directly contributes to the build failure.
            - Provide a brief summary of the current build error, including its cause.
            - Offer a high-level explanation of how to resolve the error.
            - Return only their paths in a JSON array under "suspectFiles".

            Output format:
            {{
              "errorSummary": "<brief summary of error>",
              "cause": "<error cause explanation>",
              "resolutionHint": "<high-level fix suggestion>",
              "suspectFiles": [
                {{ "path": "<repo-relative file path>" }},
                ...
              ]
            }}

            Requirements:
            - Output must be **one JSON object** with exactly four keys: errorSummary, cause, resolutionHint, suspectFiles.
            - Do not include any other fields.
            - Do **not** wrap output in markdown, code fences, or extra commentary.
            - Only include files that actually participate in the failure.

        """
        return await ask_gpt(prompt)
