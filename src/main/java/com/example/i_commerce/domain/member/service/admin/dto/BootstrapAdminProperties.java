package com.example.i_commerce.domain.member.service.admin.dto;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record BootstrapAdminProperties(
    boolean enabled,
    String email,
    String password,
    String name
) {

}