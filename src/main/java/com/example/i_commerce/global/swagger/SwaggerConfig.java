package com.example.i_commerce.global.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String securitySchemename = "JWT";

        return new OpenAPI()
            .info(new Info()
                .title("i-commerce API")
                .description("i-commerce 프로젝트 API 문서")
                .version("vq"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemename))
            .components(new Components()
                .addSecuritySchemes(securitySchemename,
                    new SecurityScheme()
                        .name(securitySchemename)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Jwt")));
    }
}
