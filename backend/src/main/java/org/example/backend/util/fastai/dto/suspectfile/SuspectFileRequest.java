package org.example.backend.util.fastai.dto.suspectfile;

import lombok.*;
import org.example.backend.domain.gitlab.dto.GitlabCompareDiff;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspectFileRequest {
    private List<GitlabCompareDiff> gitDiff;
    private String jenkinsLog;
    private List<String> applicationNames;
}