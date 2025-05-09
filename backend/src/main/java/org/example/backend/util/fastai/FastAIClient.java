package org.example.backend.util.fastai;

import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.util.fastai.dto.suspectapp.InferAppRequest;
import org.example.backend.util.fastai.dto.aireport.ReportResponseDto;
import org.example.backend.util.fastai.dto.resolvefile.ResolveErrorResponseDto;
import org.example.backend.util.fastai.dto.suspectfile.SuspectFileResponseDto;

import java.util.List;

public interface FastAIClient {
    List<String> requestInferApplications(InferAppRequest request);

    SuspectFileResponseDto requestSuspectFiles(String diffRaw, String tree, String log);

    ResolveErrorResponseDto requestResolveError(String errorSummary, String cause, String resolutionHint, String filesRawJson);

    String requestPatchText(String originalCode, String instruction);

    PatchedFile requestPatchFile(String path, String originalCode, String instruction);

    ReportResponseDto requestErrorReport(String jsonBody);
}