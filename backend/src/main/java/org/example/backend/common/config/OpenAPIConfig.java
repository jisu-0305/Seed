package org.example.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI api() {
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("JWT");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("JWT", jwtScheme))
                .addSecurityItem(securityRequirement);
    }
}