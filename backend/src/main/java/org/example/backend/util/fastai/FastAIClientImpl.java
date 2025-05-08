package org.example.backend.util.fastai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class FastAIClientImpl implements FastAIClient{

    private final WebClient webClient;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

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
