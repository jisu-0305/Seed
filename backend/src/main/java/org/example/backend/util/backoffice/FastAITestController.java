package org.example.backend.util.backoffice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.fastai.FastAIClientImpl;
import org.example.backend.util.fastai.dto.*;
import org.example.backend.util.fastai.dto.aireport.ReportResponseDto;
import org.example.backend.util.fastai.dto.resolvefile.ResolveErrorResponseDto;
import org.example.backend.util.fastai.dto.suspectapp.InferAppRequest;
import org.example.backend.util.fastai.dto.suspectfile.FilepathRequest;
import org.example.backend.util.fastai.dto.suspectfile.SuspectFileResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/test/fastai")
@RequiredArgsConstructor
@Slf4j
public class FastAITestController {

    private final FastAIClientImpl fastAIClientImpl;

    @PostMapping("/infer")
    public ResponseEntity<List<String>> testInferAppRequest(@RequestBody InferAppRequest request) {
        List<String> response = fastAIClientImpl.requestInferApplications(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filepath")
    public ResponseEntity<SuspectFileResponseDto> testFilepathRequest(@ModelAttribute FilepathRequest filepathRequest) {
        if (!StringUtils.hasText(filepathRequest.getDiffRaw()) ||
                !StringUtils.hasText(filepathRequest.getTree()) ||
                !StringUtils.hasText(filepathRequest.getLog())) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        SuspectFileResponseDto response = fastAIClientImpl.requestSuspectFiles(
                filepathRequest.getDiffRaw(),
                filepathRequest.getTree(),
                filepathRequest.getLog()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resolve")
    public ResponseEntity<ResolveErrorResponseDto> testResolveRequest(@ModelAttribute ResolveRequest resolveRequest) {
        ResolveErrorResponseDto response = fastAIClientImpl.requestResolveError(
                resolveRequest.getErrorSummary(),
                resolveRequest.getCause(),
                resolveRequest.getResolutionHint(),
                resolveRequest.getFilesRaw()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patch")
    public ResponseEntity<String> testPatchText(@ModelAttribute PatchTextRequest patchTextRequest) {
        String instruction = fastAIClientImpl.requestPatchText(
                patchTextRequest.getOriginalCode(),
                patchTextRequest.getInstruction()
        );
        return ResponseEntity.ok(instruction);
    }

    @PostMapping("/patch/file")
    public ResponseEntity<PatchedFile> testPatchFile(@ModelAttribute PatchFileRequest patchFileRequest) {
        PatchedFile patchedFile = fastAIClientImpl.requestPatchFile(
                patchFileRequest.getPath(),
                patchFileRequest.getOriginalCode(),
                patchFileRequest.getInstruction()
        );
        return ResponseEntity.ok(patchedFile);
    }

    @PostMapping("/report")
    public ResponseEntity<ReportResponseDto> testReport(@RequestBody String reportJson) {
        ReportResponseDto response = fastAIClientImpl.requestErrorReport(reportJson);
        return ResponseEntity.ok(response);
    }
}
