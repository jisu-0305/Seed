package org.example.backend.domain.gitlab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.CryptoUtil;
import org.example.backend.domain.gitlab.dto.*;
import org.example.backend.domain.gitlab.entity.GitlabToken;
import org.example.backend.domain.gitlab.repository.GitlabTokenRepository;
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
    private final GitlabTokenRepository tokenRepo;
    private final GitlabApiClient apiClient;    // ★ page/perPage 없는 새 시그니처
    private final CryptoUtil cryptoUtil;

    /* =============== PAT 등록 =============== */
    @Override
    public void registerToken(Long userId, String plainToken) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String encrypted = cryptoUtil.encrypt(plainToken);

        tokenRepo.findByUser_Id(userId).ifPresentOrElse(
                existing -> {
                    existing.changeToken(encrypted);
                    tokenRepo.save(existing);
                },
                () -> tokenRepo.save(GitlabToken.of(user, encrypted))
        );
    }

    /* =============== 프로젝트 목록 =============== */
    @Override
    public List<GitlabProjectDto> getProjects(Long userId) {
        String token = fetchToken(userId);
        log.info("▶▶▶ DECRYPTED PAT for user {}: {}", userId, token);
        List<GitlabProjectDto> projects = apiClient.listProjects(token);
        return projects;
    }

    /* =============== 레포지토리 트리 =============== */
    @Override
    public List<GitlabTreeItemDto> getTree(
            Long userId, Long projectId, String path, boolean recursive) {

        String pat = fetchToken(userId);
        return apiClient.listTree(pat, projectId, path, recursive);  // ✔ page 인자 제거
    }

    /* =============== 파일 원문 =============== */
    @Override
    public String getFile(Long userId, Long projectId, String path, String ref) {
        String pat = fetchToken(userId);
        return apiClient.getRawFile(pat, projectId, path, ref);      // ✔ 인코딩 내부 처리
    }

    /* =============== 내부 메서드 =============== */
    private String fetchToken(Long userId) {
        return tokenRepo.findByUser_Id(userId)
                .map(token -> token.getDecryptedToken(cryptoUtil))
                .orElseThrow(() -> new BusinessException(ErrorCode.OAUTH_TOKEN_FORBIDDEN));
    }
}
