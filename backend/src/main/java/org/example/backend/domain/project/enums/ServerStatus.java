package org.example.backend.domain.project.enums;

public enum ServerStatus {
    INIT,
    SET_SWAP_MEMORY,
    UPDATE_PACKAGE,
    INSTALL_JDK,
    INSTALL_DOCKER,
    RUN_APPLICATION,
    INSTALL_NGINX,
    INSTALL_JENKINS,
    INSTALL_JENKINS_PLUGINS,
    SET_JENKINS_INFO,
    CREATE_JENKINS_PIPELINE,
    CREATE_JENKINSFILE,
    CREATE_FRONTEND_DOCKERFILE,
    CREATE_BACKEND_DOCKERFILE,
    CREATE_WEBHOOK,
    FINISH,   // 자동 배포 세팅 완료
    FAIL,     // 중간 과정 실패
    //AI CICDResolver 관련
    JENKINS_BUILD_LOG,        // 1-1. Jenkins 에러 로그 수집
    COLLECTING_APP_INFO,      // 1-2. App 이름, MR Diff, Git 트리 등 수집
    INFERING_ERROR_SOURCE,    // 1-4. AI를 통해 의심 App 및 파일 추론
    COLLECTING_LOGS_AND_TREES,// 1-5~1-6.  Docker 로그 및 tree구조 수집
    SUSPECT_FILE,             // 2-2. AI suspect 파일 찾기 요청
    GET_ORIGINAL_CODE,        // 2-3. Gitlab에서 원본 코드 수집
    GET_INSTRUCTION,          // 2-4. AI 의심되는 파일의 코드 수정 지시본 수집
    GET_FIXED_CODE,           // 2-5. AI가 만든 수정된 코드 수집
    COMMITTING_FIXES,         // 3-1~3-2. GitLab 브랜치 생성 및 커밋
    JENKINS_REBUILDING,        // 3-3. Jenkins로 재빌드 시작
    REBUILD_SUCCESS,          // 4-1. 재빌드 성공
    REBUILD_FAIL,             // 4-1. 재빌드 실패
    CREATING_REPORT,          // 4-2. AI 보고서 생성
    CREATE_PULL_REQUEST,      // 4-3 GitLab PR날리기
    SAVING_REPORT,            // 4-4. AI 보고서 저장
    FINISH_WITH_AI,        // 최종 완료, 배포 성공
    FAIL_WTIH_AI,          // 최종 완료, 배포 실패
    BUILD_FAIL_WITH_AI,    // 2번째 기능 중간과정 실패

    // Https 세팅 관련
    INSTALL_CERTBOT,
    CREATE_NGINX_CONFIGURATION_FILE,
    RELOAD_NGINX,
    ISSUE_SSL_CERTIFICATE,
    EDIT_NGINX_CONFIGURATION_FILE,
    FINISH_CONVERT_HTTPS,
    FAIL_HTTPS
}
