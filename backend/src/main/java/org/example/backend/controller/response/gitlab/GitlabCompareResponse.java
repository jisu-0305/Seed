package org.example.backend.controller.response.gitlab;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.domain.gitlab.dto.GitlabCompareCommit;
import org.example.backend.domain.gitlab.dto.GitlabCompareDiff;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GitlabCompareResponse {
    private GitlabCompareCommit commit; // 마지막 커밋 하나만
    private List<GitlabCompareCommit> commits; // from 부터 to 직전 또는 to 까지 포함한 변경 이력들
    private List<GitlabCompareDiff> diffs; // from~to 사이에 변경된 각 파일의 변경정보
    private boolean compareTimeout;
    private boolean compareSameRef; // true 라는말 == from 이랑 to가 같은 리비전임 == 변경된게 없으니까 commits, diffs 가 빈값임
    private String webUrl; // gitlab 에서 diff 볼 수 있는 url
}
