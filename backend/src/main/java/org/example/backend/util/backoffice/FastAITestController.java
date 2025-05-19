package org.example.backend.util.backoffice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.aiapi.AIApiClientImpl;
import org.example.backend.util.aiapi.dto.aireport.AIReportRequest;
import org.example.backend.util.aiapi.dto.aireport.AIReportResponse;
import org.example.backend.util.aiapi.dto.patchfile.PatchFileRequest;
import org.example.backend.util.aiapi.dto.patchfile.PatchTextRequest;
import org.example.backend.util.aiapi.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.aiapi.dto.resolvefile.ResolveRequest;
import org.example.backend.util.aiapi.dto.suspectapp.InferAppRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/test/fastai")
@RequiredArgsConstructor
@Slf4j
public class FastAITestController {

    private final AIApiClientImpl fastAIClientImpl;

    @PostMapping("/infer")
    public ResponseEntity<List<String>> testInferAppRequest(@RequestBody InferAppRequest request) {
        List<String> response = fastAIClientImpl.requestInferApplications(request, request.getProjectId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filepath")
    public ResponseEntity<SuspectFileResponse> testFilepathRequest(@ModelAttribute SuspectFileRequest filepathRequest) {
        if (!StringUtils.hasText(filepathRequest.getDiffRaw()) ||
                !StringUtils.hasText(filepathRequest.getTree()) ||
                !StringUtils.hasText(filepathRequest.getLog())) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        SuspectFileResponse response = fastAIClientImpl.requestSuspectFiles(filepathRequest, filepathRequest.getProjectId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resolve")
    public ResponseEntity<ResolveErrorResponse> testResolveRequest(@ModelAttribute ResolveRequest resolveRequest) {
        ResolveErrorResponse response = fastAIClientImpl.requestResolveError(
                resolveRequest.getSuspectFileInnerResponse(),
                resolveRequest.getFilesRaw(),
                resolveRequest.getProjectId()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patch")
    public ResponseEntity<String> testPatchText(@ModelAttribute PatchTextRequest patchTextRequest) {
        String instruction = fastAIClientImpl.requestPatchText(patchTextRequest, patchTextRequest.getProjectId());
        return ResponseEntity.ok(instruction);
    }

    @PostMapping("/patch/file")
    public ResponseEntity<PatchedFile> testPatchFile(@ModelAttribute PatchFileRequest patchFileRequest) {
        PatchedFile patchedFile = fastAIClientImpl.requestPatchFile(patchFileRequest, patchFileRequest.getProjectId());
        return ResponseEntity.ok(patchedFile);
    }

    @PostMapping("/report")
    public ResponseEntity<AIReportResponse> testReport(@RequestBody AIReportRequest reportRequest) {
        AIReportResponse response = fastAIClientImpl.requestErrorReport(reportRequest, reportRequest.getProjectId());
        return ResponseEntity.ok(response);
    }
}
