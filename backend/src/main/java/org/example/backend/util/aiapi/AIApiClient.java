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
    List<String> requestInferApplications(InferAppRequest inferAppRequest, Long projectId);

    SuspectFileResponse requestSuspectFiles(SuspectFileRequest suspectFileRequest, Long projectId);

    ResolveErrorResponse requestResolveError(SuspectFileInnerResponse suspectFileInnerResponse, String filesRawJson, Long projectId);

    String requestPatchText(PatchTextRequest patchTextRequest, Long projectId);

    PatchedFile requestPatchFile(PatchFileRequest patchFileRequest, Long projectId);

    AIReportResponse requestErrorReport(AIReportRequest aiReportRequest, Long projectId);
}