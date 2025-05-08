package org.example.backend.util.fastai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.util.fastai.dto.InferAppRequest;
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

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    public List<String> requestInferApplications(InferAppRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);

            String response = webClient.post()
                    .uri(fastApiBaseUrl + "/ai/infer/apps")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 🔥 suspectedApps 검증 추가
            JsonNode root = objectMapper.readTree(response);
            JsonNode suspected = root.get("suspectedApps");

            if (suspected == null || !suspected.isArray() || suspected.isEmpty()) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "AI 응답에 의심 애플리케이션이 없습니다.");
            }

            return objectMapper.convertValue(suspected, new TypeReference<List<String>>() {});
        } catch (BusinessException be) {
            throw be; // 그대로 던짐
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 추론 호출 중 오류 발생", e);
        }
    }

    public String requestSuspectFiles(String diffRaw, String tree, String log) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("diff_raw", diffRaw);
        formData.add("tree", tree);
        formData.add("log", log);

        return webClient.post()
                .uri(fastApiBaseUrl + "/ai/filepath")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String requestResolveError(String errorSummary, String cause, String resolutionHint, String filesRawJson) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("errorSummary", errorSummary);
        formData.add("cause", cause);
        formData.add("resolutionHint", resolutionHint);
        formData.add("files_raw", filesRawJson);

        return webClient.post()
                .uri(fastApiBaseUrl + "/ai/resolve")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String requestPatchText(String originalCode, String instruction) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("original_code", originalCode);
        formData.add("instruction", instruction);

        return webClient.post()
                .uri(fastApiBaseUrl + "/ai/patch")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String requestPatchFile(String path, String originalCode, String instruction) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("path", path);
        formData.add("original_code", originalCode);
        formData.add("instruction", instruction);

        return webClient.post()
                .uri(fastApiBaseUrl + "/ai/patch/file")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String requestErrorReport(String jsonBody) {
        return webClient.post()
                .uri(fastApiBaseUrl + "/ai/report")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
