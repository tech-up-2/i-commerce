package com.example.i_commerce.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // 전체 공개 API
                .requestMatchers(
                    "/api/v1/products/**",
                    "/api/v1/categories/**"
                ).permitAll()

                // 로그인 / 회원가입도 보통 공개
                .requestMatchers(
                    "/api/v1/auth/**"
                ).permitAll()

                // 리뷰 조회 공개
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()

                // 나머지는 기본적으로 인증 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}