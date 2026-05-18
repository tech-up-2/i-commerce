package com.example.i_commerce.global.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    private OpenApiCustomizer securityCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();

            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            components.addSecuritySchemes(
                "BearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            );
        };
    }

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
            .group("Chat")
            .pathsToMatch(
                "/api/v1/chat/**"
            )
            .addOpenApiCustomizer(securityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
            .group("Member")
            .pathsToMatch(
                "/api/v1/auth/**",
                "/api/v1/admin/**",
                "/api/v1/members/delivery-addresses",
                "/api/v1/members/delivery-addresses/**"
            )
            .addOpenApiCustomizer(securityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
            .group("Order")
            .pathsToMatch(
                "/api/v1/orders/**",
                "/api/v1/payments/**"
            )
            .addOpenApiCustomizer(securityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
            .group("Product")
            .pathsToMatch(
                "/api/v1/seller/products"
            )
            .addOpenApiCustomizer(securityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi cartApi() {
        return GroupedOpenApi.builder()
            .group("Cart")
            .pathsToMatch(
                "/api/v1/carts/**"
            )
            .addOpenApiCustomizer(securityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder()
            .group("Review")
            .pathsToMatch(
                "/api/v1/reviews",
                "/api/v1/reviews/**"
            )
            .addOpenApiCustomizer(securityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi s3ImageApi() {
        return GroupedOpenApi.builder()
                .group("s3Image-test")
                .pathsToMatch(
                        "/api/v1/images/**"
                )
                .addOpenApiCustomizer(securityCustomizer())
                .build();
    }
}
