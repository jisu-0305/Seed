package org.example.backend.util.aiapi;

import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.util.aiapi.dto.aireport.AIReportRequest;
import org.example.backend.util.aiapi.dto.patchfile.PatchFileRequest;
import org.example.backend.util.aiapi.dto.patchfile.PatchTextRequest;
import org.example.backend.util.aiapi.dto.suspectapp.InferAppRequest;
import org.example.backend.util.aiapi.dto.aireport.AIReportResponse;
import org.example.backend.util.aiapi.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileInnerResponse;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileResponse;

import java.util.List;

public interface AIApiClient {
    List<String> requestInferApplications(InferAppRequest inferAppRequest);

    SuspectFileResponse requestSuspectFiles(SuspectFileRequest suspectFileRequest);

    ResolveErrorResponse requestResolveError(SuspectFileInnerResponse suspectFileInnerResponse, String filesRawJson);

    String requestPatchText(PatchTextRequest patchTextRequest);

    PatchedFile requestPatchFile(PatchFileRequest patchFileRequest);

    AIReportResponse requestErrorReport(AIReportRequest aiReportRequest);
}