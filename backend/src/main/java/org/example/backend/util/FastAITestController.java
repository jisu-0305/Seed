package org.example.backend.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.util.fastai.FastAIAgent;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/test/fastai")
@RequiredArgsConstructor
@Slf4j
public class FastAITestController {

    private final FastAIAgent fastAIAgent;

    @PostMapping("/filepath")
    public ResponseEntity<String> testFilepathRequest(@ModelAttribute SuspectFileRequestDto request) {
        if (!StringUtils.hasText(request.getDiffRaw()) ||
                !StringUtils.hasText(request.getTree()) ||
                !StringUtils.hasText(request.getLog())) {
            return ResponseEntity.badRequest().body("All fields (diffRaw, tree, log) are required.");
        }

        String response = fastAIAgent.requestSuspectFiles(
                request.getDiffRaw(),
                request.getTree(),
                request.getLog()
        );
        return ResponseEntity.ok(response);
    }
}
