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
public class FastAIAgent {

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
}
