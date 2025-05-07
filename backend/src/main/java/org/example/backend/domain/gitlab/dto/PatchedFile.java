package org.example.backend.domain.gitlab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatchedFile {
    private String path; // 저장소 내 파일 경로 (src/main/java/com/example/Foo.java)
    private String patchedCode; // AI가 생성한 수정된 전체 파일 내용
    private String encoding = "text"; // 인코딩(안써도 됨): 기본 text, base64일 경우 'base64'
}
