package org.example.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 1xxx: 파라미터 관련 오류 
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 1001, "잘못된 파라미터입니다."),
    INVALID_AUTHORIZATION_HEADER(HttpStatus.BAD_REQUEST, 1102, "Authorization 헤더 형식이 잘못되었습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 1103, "지원하지 않는 요청입니다."),

    // 2xxx: 비즈니스 로직 관련 오류
    BUSINESS_ERROR(HttpStatus.BAD_REQUEST, 2001, "비즈니스 로직 오류가 발생했습니다."),
    INVALID_STEP_ID(HttpStatus.BAD_REQUEST, 2002, "stepId는 정수여야 합니다"),

    // 3xxx: 리소스(자원) 관련 오류
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 3001, "요청한 자원을 찾을 수 없습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, 3002, "프로젝트를 찾을 수 없습니다."),
    PROJECT_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, 3003, "프로젝트 상태 정보를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 3101, "사용자를 찾을 수 없습니다."),
    OAUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, 3102, "Oauth 서버에서 사용자를 찾을 수 없습니다."),
    GITLAB_BAD_REQUEST(HttpStatus.BAD_REQUEST, 3203, "GitLab API 요청이 실패했습니다."),
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, 3204, "초대를 찾을 수 없습니다."),
    ALREADY_JOINED_PROJECT(HttpStatus.BAD_REQUEST, 3205, "이미 참여 중인 프로젝트입니다."),
    DUPLICATE_INVITATION(HttpStatus.BAD_REQUEST, 3206, "이미 초대된 사용자입니다."),
    CANNOT_INVITE_SELF(HttpStatus.BAD_REQUEST, 3207, "자기 자신에게는 초대를 보낼 수 없습니다."),
    USER_PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, 3208, "프로젝트에 참여하지 않은 사용자입니다."),
    OAUTH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, 3209, "Oauth Token이 존재하지 않습니다."),
    GITLAB_BAD_PROJECTS(HttpStatus.BAD_REQUEST, 3210, "GITLAB 프로젝트 API 요청이 실패했습니다."),
    GITLAB_BAD_TREE(HttpStatus.BAD_REQUEST, 3211, "GITLAB 디렉토리 구조 API 요청이 실패했습니다."),
    GITLAB_BAD_FILE(HttpStatus.BAD_REQUEST, 3212, "GITLAB 파일 API 요청이 실패했습니다."),
    DOCKER_SEARCH_FAILED(HttpStatus.BAD_REQUEST, 3213, "도커 이미지 검색이 실패했습니다."),
    DOCKER_TAGS_FAILED(HttpStatus.BAD_REQUEST, 3214, "도커 이미지 태그 검색이 실패했습니다."),
    DOCKER_PORTS_FAILED(HttpStatus.BAD_REQUEST, 3215, "default port를 불러오는 데 싫패했습니다"),
    DOCKER_SEARCH_API_FAILED(HttpStatus.BAD_REQUEST, 3216, "도커 이미지 검색 api 요청이 실패했습니다."),
    DOCKER_TAGS_API_FAILED(HttpStatus.BAD_REQUEST, 3217, "도커 이미지 태그 검색 api 요청이 실패했습니다."),
    GITLAB_BAD_COMPARE(HttpStatus.BAD_REQUEST, 3218, "깃랩 diff를 불러오는 데 실패했습니다."),
    GITLAB_BAD_CREATE_BRANCH(HttpStatus.BAD_REQUEST, 3219, "깃랩 브랜치 생성에 실패했습니다."),
    GITLAB_BAD_DELETE_BRANCH(HttpStatus.BAD_REQUEST, 3220, "깃랩 브랜치 삭제를 실패했습니다."),
    PROJECT_CONFIG_NOT_FOUND(HttpStatus.BAD_REQUEST, 3221, "프로젝트 설정 정보를 찾지 못했습니다."),
    UNSUPPORTED_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, 3221, "지원하지 않는 알림 타입입니다."),
    GITLAB_MERGE_REQUEST_FAILED(HttpStatus.BAD_REQUEST, 3222, "깃랩 mr 생성을 실패했습니다."),
    GITLAB_BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, 3223, "해당 브랜치를 조회할 수 없습니다."),
    GITLAB_BAD_CREATE_WEBHOOK(HttpStatus.BAD_REQUEST, 3224, "깃랩 웹훅 생성에 실패했습니다."),
    DOCKER_HEALTH_API_FAILED(HttpStatus.BAD_REQUEST, 3225, "도커 소켓 API 연결에 실패했습니다."),
    DOCKER_HEALTH_FAILED(HttpStatus.BAD_REQUEST, 3226, "도커 소켓 연결에 실패했습니다."),
    GITLAB_BAD_MERGE_REQUESTS(HttpStatus.BAD_REQUEST, 3227, "깃랩 Merge Requests 조회에 실패했습니다."),
    GITLAB_MR_NOT_FOUND(HttpStatus.NOT_FOUND, 3228, "해당 Merge Request를 찾을 수 없습니다."),
    GITLAB_NO_MERGE_REQUESTS(HttpStatus.NOT_FOUND,  3229, "해당 프로젝트에 Merge Request가 없습니다."),
    GITLAB_BAD_CREATE_COMMIT(HttpStatus.BAD_REQUEST, 3230, "깃랩 커밋 생성에 실패했습니다."),
    DOCKER_LOGS_API_FAILED(HttpStatus.BAD_REQUEST, 3231, "도커 로그 조회에 실패했습니다."),
    DOCKER_DEFAULT_PORT_API_FAILED(HttpStatus.BAD_REQUEST, 3232, "도커 default port 조회에 실패했습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, 3233, "어플리케이션 정보를 찾지 못했습니다."),
    PEM_NOT_FOUND(HttpStatus.NOT_FOUND, 3234, "PemFile 정보를 찾지 못했습니다."),
    BACK_ENV_NOT_FOUND(HttpStatus.NOT_FOUND, 3235, "frontEnvFile 정보를 찾지 못했습니다."),
    FRONT_ENV_NOT_FOUND(HttpStatus.NOT_FOUND, 3236, "bacakendEnvFile 정보를 찾지 못했습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, 3237, "AI report정보를 찾지 못했습니다."),
    DOCKER_CONTROL_FAILED(HttpStatus.NOT_FOUND, 3238, "도커 컨테이너 제어에 실패했습니다."),


    // 4xxx: 인증/권한 관련 오류
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4001, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 4002, "권한이 없습니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, 4003, "유효하지 않은 사용자입니다."),
    OAUTH_TOKEN_FORBIDDEN(HttpStatus.NOT_FOUND, 4101, "Oauth 토큰 발급에 실패했습니다."),
    DOCKER_TOKEN_API_FAILED(HttpStatus.NOT_FOUND, 4102, "docker 토큰 발급에 실패했습니다."),

    // 6xxx: 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 6001, "내부 서버 오류입니다."),
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6002, "FCM 메시지 전송에 실패했습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6003, "파일 저장에 실패했습니다."),
    JENKINS_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6004, "젠킨스 API 요청이 실패했습니다."),
    JENKINS_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6005, "젠킨스 응답 파싱에 실패했습니다."),
    COMMAND_EXECUTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6006, "서버 명령어 실행에 실패했습니다."),
    CERTBOT_ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6007, "SSL 인증서 발급에 실패했습니다."),
    NGINX_RELOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6008, "Nginx reload에 실패했습니다."),
    JENKINS_TOKEN_ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6009, "Jenkins API 토큰 발급에 실패했습니다."),
    JENKINS_TOKEN_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6010, "Jenkins API 토큰 저장에 실패했습니다."),
    JENKINS_CRUMB_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,6011, "Jenkins Crumb 요청 실패"),
    JENKINS_TOKEN_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR,6012, "Jenkins Token 응답이 JSON이 아님"),
    JENKINS_TOKEN_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,6013, "Jenkins Token 파싱 실패"),
    JENKINS_TOKEN_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,6014, "Jenkins Token 요청 실패"),
    JENKINS_INFO_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,6015, "해당 프로젝트의 Jenkins 정보가 존재하지 않습니다."),
    JENKINS_STEP_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,6016, "해당 step 번호에 대한 로그를 찾을 수 없습니다."),
    HTTPS_ALREADY_ENABLED(HttpStatus.INTERNAL_SERVER_ERROR,6017, "이미 HTTPS가 활성화된 프로젝트입니다."),
    AI_INFER_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6018, "AI 추론 요청 직렬화에 실패했습니다."),
    AI_INFER_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6019, "AI 추론 응답 파싱에 실패했습니다."),
    AI_FILEPATH_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6020, "AI 파일 위치 추론 요청 실패"),
    AI_FILEPATH_RESPONSE_VALIDATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6021, "AI 파일 위치 응답 필드 유효성 검사 실패"),
    AI_RESOLVE_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6022, "AI 오류 해결 요청 실패"),
    AI_RESOLVE_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, 6023, "AI 오류 해결 응답 파싱 또는 필드 검사 실패"),
    AI_PATCH_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6024, "AI 패치 생성 요청 실패"),
    AI_PATCH_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6025, "AI 패치 응답 파싱 실패"),
    AI_REPORT_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6026, "AI 보고서 생성 요청 실패"),
    AI_REPORT_RESPONSE_MALFORMED(HttpStatus.INTERNAL_SERVER_ERROR, 6027, "AI 보고서 응답 파싱 또는 필드 누락"),
    AI_COMMUNICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6028, "AI API 요청간 에러 발생"),
    AI_RESPONSE_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6029, "AI 응답 변환 과정 에러");

    private final HttpStatus status;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
