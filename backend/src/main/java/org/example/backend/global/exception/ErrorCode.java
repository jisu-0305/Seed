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
    GITLAB_BAD_REQUEST(HttpStatus.BAD_REQUEST, 3201, "GitLab API 요청이 실패했습니다."),
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, 3201, "초대를 찾을 수 없습니다."),
    ALREADY_JOINED_PROJECT(HttpStatus.BAD_REQUEST, 3202, "이미 참여 중인 프로젝트입니다."),
    DUPLICATE_INVITATION(HttpStatus.BAD_REQUEST, 3203, "이미 초대된 사용자입니다."),
    CANNOT_INVITE_SELF(HttpStatus.BAD_REQUEST, 3204, "자기 자신에게는 초대를 보낼 수 없습니다."),

    // 4xxx: 인증/권한 관련 오류
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4001, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 4002, "권한이 없습니다."),
    OAUTH_TOKEN_FORBIDDEN(HttpStatus.NOT_FOUND, 4101, "Oauth 토큰 발급에 실패했습니다."),

    // 6xxx: 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 6001, "내부 서버 오류입니다.");

    private final HttpStatus status;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
