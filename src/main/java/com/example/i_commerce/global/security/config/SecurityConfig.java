package com.example.i_commerce.global.security.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                //임시 권한 추가
                .requestMatchers(
                    "/connect",
                    "/connect/**"
                ).permitAll()


                // 전체 공개 API
                .requestMatchers(
                    "/api/products/**",
                    "/api/categories/**"
                ).permitAll()

                // 로그인 / 회원가입도 보통 공개
                .requestMatchers(
                    "/api/auth/**"
                ).permitAll()
                .anyRequest().permitAll()

                // 나머지는 기본적으로 인증 필요
//                .anyRequest().authenticated()
            );

        return http.build();
    }

}
