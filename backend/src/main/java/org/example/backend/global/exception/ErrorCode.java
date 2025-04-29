package org.example.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 1xxx: 파라미터 관련 오류
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 1001, "잘못된 파라미터입니다."),
    INVALID_AUTHORIZATION_HEADER(HttpStatus.BAD_REQUEST, 1101, "Authorization 헤더 형식이 잘못되었습니다."),

    // 2xxx: 비즈니스 로직 관련 오류
    BUSINESS_ERROR(HttpStatus.BAD_REQUEST, 2001, "비즈니스 로직 오류가 발생했습니다."),

    // 3xxx: 리소스(자원) 관련 오류
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 3001, "요청한 자원을 찾을 수 없습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, 3002, "프로젝트를 찾을 수 없습니다."),
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

    UNSUPPORTED_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, 3221, "지원하지 않는 알림 타입입니다."),

    GITLAB_MERGE_REQUEST_FAILED(HttpStatus.BAD_REQUEST, 3222, "깃랩 mr 생성을 실패했습니다."),
    GITLAB_BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, 3223, "해당 브랜치를 조회할 수 없습니다."),

    GITLAB_BAD_CREATE_WEBHOOK(HttpStatus.BAD_REQUEST, 3224, "깃랩 웹훅 생성에 실패했습니다."),

    // 4xxx: 인증/권한 관련 오류
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4001, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 4002, "권한이 없습니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, 4003, "유효하지 않은 사용자입니다."),
    OAUTH_TOKEN_FORBIDDEN(HttpStatus.NOT_FOUND, 4101, "Oauth 토큰 발급에 실패했습니다."),

    // 6xxx: 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 6001, "내부 서버 오류입니다."),
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6002, "FCM 메시지 전송에 실패했습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6003, "파일 저장에 실패했습니다."),
    JENKINS_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6004, "젠킨스 API 요청이 실패했습니다."),
    JENKINS_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6005, "젠킨스 응답 파싱에 실패했습니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
