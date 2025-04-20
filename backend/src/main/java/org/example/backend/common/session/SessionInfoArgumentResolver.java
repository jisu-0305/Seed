/* src/main/java/org/example/backend/common/session/SessionInfoArgumentResolver.java */
package org.example.backend.common.session;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.jwt.JwtTokenProvider;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class SessionInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;   // stateless

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SessionInfo.class) &&
                parameter.getParameterType().equals(SessionInfoDto.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {

        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);

        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.INVALID_AUTHORIZATION_HEADER);
        }

        String jwt = auth.substring(7);

        if (!jwtTokenProvider.validateToken(jwt)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        /* --- JWT Claims 추출 --- */
        Long userId      = Long.valueOf(jwtTokenProvider.getSubjectFromToken(jwt));
        String oauthId   = jwtTokenProvider.getClaim(jwt, "oauthUserId", String.class);
        String provider  = jwtTokenProvider.getClaim(jwt, "provider", String.class);

        return SessionInfoDto.builder()
                .userId(userId)
                .oauthId(oauthId)
                .providerType(org.example.backend.domain.user.enums.ProviderType.valueOf(provider))
                .build();
    }
}
