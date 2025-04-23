package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.session.RedisSessionManager;
import org.example.backend.common.session.dto.SessionInfoDto;
import org.example.backend.controller.request.gitlab.ProjectUrlRequest;
import org.example.backend.controller.response.gitlab.GitlabCompareResponse;
import org.example.backend.domain.gitlab.dto.GitlabBranch;
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
    private final GitlabApiClient gitlabApiClient;
    private final RedisSessionManager redisSessionManager;

    @Override
    public List<GitlabProject> getProjects(String accessToken) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.listProjects(user.getAccessToken());
    }

    @Override
    public GitlabProject getProjectInfo(String accessToken, ProjectUrlRequest request) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.getProjectInfo(user.getAccessToken(), request.getRepositoryUrl());
    }

    @Override
    public List<GitlabTree> getTree(String accessToken, Long projectId, String path, boolean recursive) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.listTree(user.getAccessToken(), projectId, path, recursive);
    }

    @Override
    public String getFile(String accessToken, Long projectId, String path, String ref) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.getRawFile(user.getAccessToken(), projectId, path, ref);
    }

    @Override
    public GitlabCompareResponse getDiff(String accessToken, Long projectId, String from, String to) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.compareCommits(user.getAccessToken(), projectId, from, to);
    }

    @Override
    public GitlabBranch createBranch(String accessToken, Long projectId, String branch, String ref) {
        SessionInfoDto session = redisSessionManager.getSession(accessToken);
        Long userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getAccessToken().isBlank()) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        return gitlabApiClient.createBranch(user.getAccessToken(), projectId, branch, ref);
    }

}
