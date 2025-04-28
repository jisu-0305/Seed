from app.core.settings import settings
import os
from fastapi import HTTPException, UploadFile
from PIL import Image, ExifTags, UnidentifiedImageError
import pillow_heif
import base64
import io
from uuid import uuid4

UPLOAD_DIR = settings.UPLOAD_DIR
os.makedirs(UPLOAD_DIR, exist_ok=True)

def extract_exif_data(image: Image.Image) -> dict:
    exif = image.getexif()
    if not exif:
        return {}
    exif_data = {}
    for tag_id, value in exif.items():
        tag = ExifTags.TAGS.get(tag_id, tag_id)
        try:
            exif_data[tag] = str(value)
        except Exception:
            exif_data[tag] = "unreadable"
    return exif_data

async def save_image(file: UploadFile) -> dict:
    ext = file.filename.split(".")[-1].lower()
    
    if ext not in {"jpg", "jpeg", "png", "gif", "heic"}:
        raise HTTPException(status_code=400, detail="지원하지 않는 이미지 형식입니다. (jpg, png, gif, heic 가능)")

    is_heic = ext == "heic"
    final_ext = "jpg" if is_heic else ext
    filename = f"{uuid4()}.{final_ext}"
    file_path = os.path.join(UPLOAD_DIR, filename)

    try:
        contents = await file.read()

        if is_heic:
            heif_file = pillow_heif.read_heif(contents)
            image = Image.frombytes(
                heif_file.mode, heif_file.size, heif_file.data, "raw"
            )
            image = image.convert("RGB")
            image.save(file_path, format="JPEG")
        else:
            with open(file_path, "wb") as f:
                f.write(contents)

            # 이미지 열기
            image = Image.open(file_path)

            # GIF일 경우 첫 프레임만 사용 (애니메이션 프레임 여러 개면 오류 방지)
            if image.format == "GIF":
                image.seek(0)
                image = image.convert("RGB")  # exif나 size 정보를 위해 RGB 변환 (필요 시)

        return {
            "filename": filename,
            "content_type": file.content_type,
            "width": image.width,
            "height": image.height,
            "path": file_path.replace("\\", "/"),
            "exif": extract_exif_data(image)
        }

    except UnidentifiedImageError:
        raise HTTPException(status_code=400, detail="이미지를 열 수 없습니다. 올바른 형식의 이미지를 업로드해주세요.")
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"이미지 저장 중 오류 발생: {str(e)}")
    

