package org.example.backend.domain.gitlab.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitlabTreeItemDto {
    private String id;
    private String name; // 파일 또는 디렉토리 이름
    private String path; // 루트기준 path
    private String type; // tree: 폴더, blob: 파일
    private String mode; // 깃 모드(permission+타이ㅂ) -> 040000:tree(디렉토리), 100644:일반차일, 100755:실행파일
}
