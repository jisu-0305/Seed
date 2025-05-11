package org.example.backend.util.fastai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.gitlab.dto.PatchedFile;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.fastai.dto.aireport.AIReportRequest;
import org.example.backend.util.fastai.dto.suspectapp.InferAppRequest;
import org.example.backend.util.fastai.dto.aireport.ReportResponse;
import org.example.backend.util.fastai.dto.resolvefile.ResolveErrorResponse;
import org.example.backend.util.fastai.dto.suspectapp.InferAppResponse;
import org.example.backend.util.fastai.dto.suspectfile.SuspectFileResponse;
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
public class FastAIClientImpl implements FastAIClient{

    private final WebClient webClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Override
    public List<String> requestInferApplications(InferAppRequest request) {
        String json;

        try {
            json = objectMapper.writeValueAsString(request);
            log.debug("🔍 [InferApp] Request JSON: {}", json);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED);
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
            throw new BusinessException(ErrorCode.AI_COMMUNICATION_FAILED);
        }


        InferAppResponse dto;
        try {
            dto = objectMapper.readValue(response, InferAppResponse.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_INFER_RESPONSE_PARSING_FAILED);
        }

        if (dto.getSuspectedApps() == null || dto.getSuspectedApps().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_INFER_RESPONSE_PARSING_FAILED);
        }

        return dto.getSuspectedApps();
    }

    @Override
    public SuspectFileResponse requestSuspectFiles(String diffRaw, String tree, String appLog) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("diff_raw", diffRaw);
        formData.add("tree", tree);
        formData.add("log", appLog);

//        log.debug(">>>>>>>> [Fast API]requestSuspectFiles: {}", formData);
        String response = webClient.post()
                .uri(fastApiBaseUrl + "/ai/filepath")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            SuspectFileResponse dto = objectMapper.readValue(response, SuspectFileResponse.class);

            // 필수 필드 확인
            if (dto.getResponse() == null ||
                    dto.getResponse().getErrorSummary() == null ||
                    dto.getResponse().getCause() == null ||
                    dto.getResponse().getResolutionHint() == null ||
                    dto.getResponse().getSuspectFiles() == null ||
                    dto.getResponse().getSuspectFiles().isEmpty()) {

                throw new BusinessException(ErrorCode.AI_FILEPATH_RESPONSE_VALIDATION_FAILED);
            }

            return dto;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_SERIALIZATION_FAILED);
        }
    }

    @Override
    public ResolveErrorResponse requestResolveError(String errorSummary, String cause, String resolutionHint, String filesRawJson) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("errorSummary", errorSummary);
        formData.add("cause", cause);
        formData.add("resolutionHint", resolutionHint);
        formData.add("files_raw", filesRawJson);

        log.debug(">>>>>>>> [Fast API]requestResolveError: {}", formData);
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
            throw new BusinessException(ErrorCode.AI_RESOLVE_REQUEST_FAILED);
        }

        try {
            ResolveErrorResponse dto = objectMapper.readValue(response, ResolveErrorResponse.class);

            if (dto.getResponse() == null ||
                    dto.getResponse().getFileFixes() == null ||
                    dto.getResponse().getFileFixes().isEmpty()) {
                throw new BusinessException(ErrorCode.AI_RESOLVE_RESPONSE_INVALID);
            }

            return dto;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_SERIALIZATION_FAILED);
        }
    }

    @Override
    public String requestPatchText(String originalCode, String instruction) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("original_code", originalCode);
        formData.add("instruction", instruction);

        log.debug(">>>>>>>> [Fast API]requestPatchText: {}", formData);
        try {
            return webClient.post()
                    .uri(fastApiBaseUrl + "/ai/patch")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_PATCH_RESPONSE_PARSING_FAILED);
        }
    }

    @Override
    public PatchedFile requestPatchFile(String path, String originalCode, String instruction) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("path", path);
        formData.add("original_code", originalCode);
        formData.add("instruction", instruction);

        log.debug(">>>>>>>> [Fast API]requestPatchFile: {}", formData);
        try {
            String patchedCode = webClient.post()
                    .uri(fastApiBaseUrl + "/ai/patch/file")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            PatchedFile patchedFile = new PatchedFile();
            patchedFile.setPath(path);
            patchedFile.setPatchedCode(patchedCode);

            return patchedFile;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_PATCH_RESPONSE_PARSING_FAILED);
        }
    }

    @Override
    public ReportResponse requestErrorReport(AIReportRequest request) {
        String response;
        String json;
        try {
            json = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_INFER_REQUEST_FAILED);
        }

        log.debug(">>>>>>>> [Fast API]requestErrorReport DTO: {}", request);

        try {
            response = webClient.post()
                    .uri(fastApiBaseUrl + "/ai/report")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_REPORT_REQUEST_FAILED);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            ReportResponse dto = mapper.readValue(response, ReportResponse.class);

            if (dto.getSummary() == null || dto.getAppliedFiles() == null || dto.getAppliedFiles().isEmpty()) {
                throw new BusinessException(ErrorCode.AI_REPORT_RESPONSE_MALFORMED);
            }

            return dto;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_REPORT_RESPONSE_MALFORMED);
        }
    }
}
