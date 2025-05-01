package org.example.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/api/**")                        // /api 로 시작하는 모든 경로
                .allowedOrigins("http://localhost:5173")      // 허용할 프론트엔드 Origin
                .allowedMethods("GET","POST","PUT","DELETE")  // 허용할 HTTP 메서드
                .allowedHeaders("*")                          // 허용할 헤더
                .allowCredentials(true);                      // 쿠키 인증이 있다면 true
    }
}
