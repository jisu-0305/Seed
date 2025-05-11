package org.example.backend.util.fastai;

import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.util.fastai.dto.suspectapp.InferAppRequest;
import org.example.backend.util.fastai.dto.aireport.ReportResponse;
import org.example.backend.util.fastai.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.fastai.dto.suspectfile.SuspectFileResponse;

import java.util.List;

public interface FastAIClient {
    List<String> requestInferApplications(InferAppRequest request);

    SuspectFileResponse requestSuspectFiles(String diffRaw, String tree, String appLog);

    ResolveErrorResponse requestResolveError(String errorSummary, String cause, String resolutionHint, String filesRawJson);

    String requestPatchText(String originalCode, String instruction);

    PatchedFile requestPatchFile(String path, String originalCode, String instruction);

    ReportResponse requestErrorReport(String jsonBody);
}