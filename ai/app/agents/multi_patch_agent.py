# app/agents/multi_patch_agent.py
from .base import BaseAgent
from app.services.openai_client import ask_gpt

class MultiPatchAgent(BaseAgent):
    async def run(self, files: dict[str, str]) -> dict[str, str]:
        """
        files: { path: full_source_code, ... }
        Returns: { path: instruction, ... }
        """
        # 1) Build prompt listing all files and their contents
        prompt = "You are an expert AI that examines multiple source files together and outputs exactly one minimal edit instruction per file.\n\n"
        for path, code in files.items():
            prompt += f"--- {path} ---\n{code}\n\n"
        prompt += (
            "Based on the above files and build error context, "
            "for each file, produce exactly one precise edit instruction "
            "that will fix the build errors when applied together.\n\n"
            "Return a single JSON object mapping each file path to its instruction, like:\n"
            '{\n'
            '  "path/to/FileA.java": "Instruction for FileA",\n'
            '  "path/to/FileB.java": "Instruction for FileB"\n'
            '}\n'
            "Do not wrap in markdown or add commentary. Use double quotes."
        )

        raw = await ask_gpt(prompt)
        return await self._parse_json(raw)

    async def _parse_json(self, raw: str) -> dict[str, str]:
        # simple JSON loader; assumes raw is valid JSON
        import json
        return json.loads(raw)
