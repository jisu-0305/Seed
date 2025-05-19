package org.example.backend.util.aiapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.domain.project.enums.ServerStatus;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.aiapi.dto.aireport.AIReportRequest;
import org.example.backend.util.aiapi.dto.patchfile.PatchFileRequest;
import org.example.backend.util.aiapi.dto.patchfile.PatchTextRequest;
import org.example.backend.util.aiapi.dto.suspectapp.InferAppRequest;
import org.example.backend.util.aiapi.dto.aireport.AIReportResponse;
import org.example.backend.util.aiapi.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.aiapi.dto.suspectapp.InferAppResponse;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileInnerResponse;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileRequest;
import org.example.backend.util.aiapi.dto.suspectfile.SuspectFileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIApiClientImpl implements AIApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Override
    public List<String> requestInferApplications(InferAppRequest inferAppRequest, Long projectId) {
        String json;

        try {
            json = objectMapper.writeValueAsString(inferAppRequest);
//            log.debug("🔍 [InferApp] Request JSON: {}", json);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }

        String response;
        try {
            response = webClient.post()
                    .uri(fastApiBaseUrl + "/ai/infer/apps")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_COMMUNICATION_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }


        InferAppResponse dto;
        try {
            dto = objectMapper.readValue(response, InferAppResponse.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_INFER_RESPONSE_PARSING_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }

        if (dto.getSuspectedApps() == null || dto.getSuspectedApps().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_INFER_RESPONSE_PARSING_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }

        return dto.getSuspectedApps();
    }

    @Override
    public SuspectFileResponse requestSuspectFiles(SuspectFileRequest suspectFileRequest, Long projectId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("diff_raw", suspectFileRequest.getDiffRaw());
        formData.add("tree", suspectFileRequest.getTree());
        formData.add("log", suspectFileRequest.getLog());

//        log.debug(">>>>>>>> [Fast API]requestSuspectFiles: {}", suspectFileRequest.getLog());
        String response = webClient.post()
                .uri(fastApiBaseUrl + "/ai/filepath")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();

//        log.debug(">>>>>>>> [Fast API]responseSupectedFiles: {}", response);

        try {
            SuspectFileResponse dto = objectMapper.readValue(response, SuspectFileResponse.class);

            // 필수 필드 확인
            if (dto.getResponse() == null ||
                    dto.getResponse().getErrorSummary() == null ||
                    dto.getResponse().getCause() == null ||
                    dto.getResponse().getResolutionHint() == null ||
                    dto.getResponse().getSuspectFiles() == null ||
                    dto.getResponse().getSuspectFiles().isEmpty()) {

                throw new BusinessException(ErrorCode.AI_FILEPATH_RESPONSE_VALIDATION_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
            }

            return dto;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_SERIALIZATION_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }
    }

    @Override
    public ResolveErrorResponse requestResolveError(SuspectFileInnerResponse suspectFileInnerResponse, String filesRawJson, Long projectId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("errorSummary", suspectFileInnerResponse.getErrorSummary());
        formData.add("cause", suspectFileInnerResponse.getCause());
        formData.add("resolutionHint", suspectFileInnerResponse.getResolutionHint());
        formData.add("files_raw", filesRawJson);

//        log.debug(">>>>>>>> [Fast API]requestResolveError: {}", formData);
        String response;
        try {
            response = webClient.post()
                    .uri(fastApiBaseUrl + "/ai/resolve")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_RESOLVE_REQUEST_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }

        try {
            ResolveErrorResponse dto = objectMapper.readValue(response, ResolveErrorResponse.class);

            if (dto.getResponse() == null ||
                    dto.getResponse().getFileFixes() == null ||
                    dto.getResponse().getFileFixes().isEmpty()) {
                throw new BusinessException(ErrorCode.AI_RESOLVE_RESPONSE_INVALID, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
            }

            return dto;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_SERIALIZATION_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }
    }

    @Override
    public String requestPatchText(PatchTextRequest patchTextRequest, Long projectId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("original_code", patchTextRequest.getOriginalCode());
        formData.add("instruction", patchTextRequest.getInstruction());

//        log.debug(">>>>>>>> [Fast API]requestPatchText: {}", formData);
        try {
            return webClient.post()
                    .uri(fastApiBaseUrl + "/ai/patch")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_PATCH_RESPONSE_PARSING_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }
    }

    @Override
    public PatchedFile requestPatchFile(PatchFileRequest patchFileRequest, Long projectId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("path", patchFileRequest.getPath());
        formData.add("original_code", patchFileRequest.getOriginalCode());
        formData.add("instruction", patchFileRequest.getInstruction());

//        log.debug(">>>>>>>> [Fast API]requestPatchFile: {}", formData);
        try {
            String patchedCode = webClient.post()
                    .uri(fastApiBaseUrl + "/ai/patch/file")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            PatchedFile patchedFile = new PatchedFile();
            patchedFile.setPath(patchFileRequest.getPath());
            patchedFile.setPatchedCode(patchedCode);

            return patchedFile;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_PATCH_RESPONSE_PARSING_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }
    }

    @Override
    public AIReportResponse requestErrorReport(AIReportRequest aiReportRequest, Long projectId) {
        String response;
        String json;
        try {
            json = objectMapper.writeValueAsString(aiReportRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }

//        log.debug(">>>>>>>> [Fast API]requestErrorReport DTO: {}", aiReportRequest);

        try {
            response = webClient.post()
                    .uri(fastApiBaseUrl + "/ai/report")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_REPORT_REQUEST_FAILED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            AIReportResponse dto = mapper.readValue(response, AIReportResponse.class);

            if (dto.getSummary() == null || dto.getAppliedFiles() == null || dto.getAppliedFiles().isEmpty()) {
                throw new BusinessException(ErrorCode.AI_REPORT_RESPONSE_MALFORMED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
            }

            return dto;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_REPORT_RESPONSE_MALFORMED, projectId, ServerStatus.BUILD_FAIL_WITH_AI);
        }
    }
}
