package org.example.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${front.base-url}")
    private String frontBaseUrl;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(frontBaseUrl));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        config.setExposedHeaders(Arrays.asList("Authorization", "Access-Control-Allow-Origin"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
