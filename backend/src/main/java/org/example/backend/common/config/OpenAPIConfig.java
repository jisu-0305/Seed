package org.example.backend.common.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${domain.name}")
    private String domainName;

    @Bean
    public OpenAPI customOpenAPI() {

        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("JWT");

        Server httpsServer = new Server()
                .url(domainName)
                .description("Production HTTPS server");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("JWT", jwtScheme))
                .addSecurityItem(securityRequirement)
                .servers(List.of(httpsServer))
                .info(new Info()
                        .title("Swagger")
                        .description("Specification")
                        .version("1.0.0"));
    }
}
