from openai import AsyncOpenAI
from app.core.settings import settings

client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)

async def ask_gpt(prompt: str, model="o4-mini") -> str:
    response = await client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": prompt}]
    )
    return response.choices[0].message.content

async def ask_gpt_stream(prompt: str, model="o4-mini"):
    response = await client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": prompt}],
        stream=True,
    )
    
    # ✅ 스트리밍 chunk 반환
    async for chunk in response:
        content = chunk.choices[0].delta.content
        if content:
            yield content

async def ask_gpt_with_images(prompt: str, base64_images: list[str]) -> str:
    image_contents = [
        {
            "type": "image_url",
            "image_url": {
                "url": f"data:image/jpeg;base64,{img}"
            }
        }
        for img in base64_images
    ]
    response = await client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": prompt},
                    *image_contents
                ]
            }
        ],
        max_tokens=1024,
    )
    return response.choices[0].message.content

async def ask_gpt_with_images_stream(prompt: str, base64_images: list[str]):
    image_contents = [
        {
            "type": "image_url",
            "image_url": {
                "url": f"data:image/jpeg;base64,{img}"
            }
        }
        for img in base64_images
    ]

    response = await client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": prompt},
                    *image_contents
                ]
            }
        ],
        max_tokens=1024,
        stream=True,
    )

    # ✅ 스트리밍 chunk 반환
    async for chunk in response:
        content = chunk.choices[0].delta.content
        if content:
            yield content

