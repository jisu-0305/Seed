package org.example.backend.util.fastai;

import org.example.backend.util.fastai.dto.InferAppRequest;

import java.util.List;

public interface FastAIClient {
    List<String> requestInferApplications(InferAppRequest request);

    String requestSuspectFiles(String diffRaw, String tree, String log);

    String requestResolveError(String errorSummary, String cause, String resolutionHint, String filesRawJson);

    String requestPatchText(String originalCode, String instruction);

    String requestPatchFile(String path, String originalCode, String instruction);

    String requestErrorReport(String jsonBody);
}