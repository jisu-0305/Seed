from .base import BaseAgent
from app.services.openai_client import ask_gpt

class BuildErrorResolverAgent(BaseAgent):
    async def run(self, errorSummary: str, cause: str, resolutionHint: str, files: list[dict]) -> str:
        prompt = f"""
            SYSTEM:
            You are BuildErrorResolverAgent, an AI specialized in diagnosing and resolving build or runtime errors.

            INPUT:
            errorSummary: {errorSummary}
            cause: {cause}
            resolutionHint: {resolutionHint}
            files: {files}   // list of {{path, code}}

            TASK:
            - Analyze errorSummary, cause, and resolutionHint.
            - Determine the minimal set of file modifications needed to resolve the error.
            For each file:
                - Identify problematic code sections.
                - Give a clear instruction: what to change in that file.
                - Briefly explain why.

            OUTPUT (JSON):
            {{
            "fileFixes": [
                {{
                "path": "<file path>",
                "instruction": "<detailed instruction for modifying the file>",
                "explanation": "<why it works>"
                }},
                …
            ],
            "resolutionReport": {{
                "errorSummary": "{errorSummary}",
                "cause": "{cause}",
                "finalResolution": "<concise applied resolution>"
            }}
            }}

            """
        return await ask_gpt(prompt)
