package org.example.backend.util.fastai;

public interface FastAIClient {
    String requestSuspectFiles(String diffRaw, String tree, String log);

    String requestResolveError(String errorSummary, String cause, String resolutionHint, String filesRawJson);

    String requestPatchText(String originalCode, String instruction);

    String requestPatchFile(String path, String originalCode, String instruction);

    String requestErrorReport(String jsonBody);
}