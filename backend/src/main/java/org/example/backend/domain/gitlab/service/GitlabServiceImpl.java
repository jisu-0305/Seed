package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.gitlab.dto.GitlabProject;
import org.example.backend.domain.gitlab.dto.GitlabTree;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitlabServiceImpl implements GitlabService {

    private final UserRepository userRepository;
    private final GitlabApiClient apiClient;

    @Override
    public List<GitlabProject> getProjects(Long userId) {
        String token = fetchToken(userId);
        log.debug(">>>>>>>>>>>>>    사용자 {} 의 GitLab 토큰: {}", userId, token);
        return apiClient.listProjects(token);
    }

    @Override
    public List<GitlabTree> getTree(Long userId, Long projectId, String path, boolean recursive) {
        String token = fetchToken(userId);
        return apiClient.listTree(token, projectId, path, recursive);
    }

    @Override
    public String getFile(Long userId, Long projectId, String path, String ref) {
        String token = fetchToken(userId);
        return apiClient.getRawFile(token, projectId, path, ref);
    }

    private String fetchToken(Long userId) {
        return userRepository.findById(userId)
                .map(User::getAccessToken) // User.accessToken 필드에서 꺼내고
                .filter(t -> !t.isBlank()) // 비어있지 않은지 확인하고
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.OAUTH_TOKEN_FORBIDDEN)); // 없거나 공백이면 예외하기
    }
}
