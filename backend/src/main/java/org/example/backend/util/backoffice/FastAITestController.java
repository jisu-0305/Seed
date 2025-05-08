package org.example.backend.util.backoffice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.util.fastai.FastAIClientImpl;
import org.example.backend.util.fastai.dto.*;
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
    public ResponseEntity<String> testFilepathRequest(@ModelAttribute FilepathRequest filepathRequest) {
        if (!StringUtils.hasText(filepathRequest.getDiffRaw()) ||
                !StringUtils.hasText(filepathRequest.getTree()) ||
                !StringUtils.hasText(filepathRequest.getLog())) {
            return ResponseEntity.badRequest().body("All fields (diffRaw, tree, log) are required.");
        }

        String response = fastAIClientImpl.requestSuspectFiles(
                filepathRequest.getDiffRaw(),
                filepathRequest.getTree(),
                filepathRequest.getLog()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resolve")
    public ResponseEntity<String> testResolveRequest(@ModelAttribute ResolveRequest resolveRequest) {
        String response = fastAIClientImpl.requestResolveError(
                resolveRequest.getErrorSummary(),
                resolveRequest.getCause(),
                resolveRequest.getResolutionHint(),
                resolveRequest.getFilesRaw()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patch")
    public ResponseEntity<String> testPatchText(@ModelAttribute PatchTextRequest patchTextRequest) {
        String response = fastAIClientImpl.requestPatchText(
                patchTextRequest.getOriginalCode(),
                patchTextRequest.getInstruction()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patch/file")
    public ResponseEntity<String> testPatchFile(@ModelAttribute PatchFileRequest patchFileRequest) {
        String response = fastAIClientImpl.requestPatchFile(
                patchFileRequest.getPath(),
                patchFileRequest.getOriginalCode(),
                patchFileRequest.getInstruction()
        );
        System.out.println("patch file response: " + response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/report")
    public ResponseEntity<String> testReport(@RequestBody String reportJson) {
        String response = fastAIClientImpl.requestErrorReport(reportJson);
        return ResponseEntity.ok(response);
    }
}
