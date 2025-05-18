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
    CREATE_JENKINS_JOB,
    CREATE_JENKINSFILE,
    CREATE_FRONTEND_DOCKERFILE,
    CREATE_BACKEND_DOCKERFILE,
    CREATE_WEBHOOK,
    FINISH,
    FAIL,
    //AI CICDResolver 관련
    WAITING_FOR_LOGIC,      // 1. 시작전 15초 대기
    JENKINS_BUILD_LOG,      // 1-1. Jenkins 에러 로그 수집 중
    COLLECTING_APP_INFO,      // 1-2. App 이름, MR Diff, Git 트리 등 수집 중
    INFERING_ERROR_SOURCE,    // 1-4~1-5. AI를 통해 의심 App 및 파일 추론 중
    COLLECTING_LOGS,          // 1-6. Docker 로그 수집 중
    FIXING_CODE,              // 2. AI 수정 코드 생성 중
    COMMITTING_FIXES,         // 3-2. GitLab 브랜치 생성 및 커밋 중
    REBUILDING,               // 3-3. Jenkins로 재빌드 시작
    REBUILD_SUCCESS,            // 4-1. 재빌드 성공
    REBUILD_FAIL,               // 4-1. 재빌드 실패
    CREATING_REPORT,          // 4-3. AI 보고서 생성 중
    SAVING_REPORT,            // 4-4. 보고서 저장 중
    COMPLETED_SUCCESSFULLY,   // 최종 완료
    COMPLETED_WITH_ERRORS     // 최종 완료
}
