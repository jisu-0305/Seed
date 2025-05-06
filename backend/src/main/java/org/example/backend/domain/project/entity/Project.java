package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.project.enums.ProjectStructure;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;
    private String projectName;
    private String serverIP;
    private LocalDateTime createdAt;
    private String repositoryUrl;

    @Enumerated(EnumType.STRING)
    private ProjectStructure structure;

    private String frontendBranchName;
    private String frontendDirectoryName;
    private String backendBranchName;
    private String backendDirectoryName;
    private String pemFilePath;

//    추가 필요 이유: 두번째 기능 jenkins 빌드 실패 요청시에 해당 프로젝트가 jenkins workflow를 통해서 api를 우리 서비스로 보낼때 인증값을 넣어주고 api를 호출해야함 상세내용 "김지수"한테 문의
//    @Column(nullable = false, unique = true)
//    private String cicdToken;
}

