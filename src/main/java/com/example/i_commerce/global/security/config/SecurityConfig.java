package com.example.i_commerce.global.security.config;

import com.example.i_commerce.global.security.jwt.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
            )
            .addFilterBefore(
                jwtAuthenticationFilter,//먼저 실행
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("*"));//모든 HTTP 메서드 접근요청 허용
        configuration.setAllowedHeaders(List.of("*"));//모든 헤더 값을 허용하겠다.
        configuration.setAllowCredentials(true);//자격증명을 허용하겠다.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);//모든 url에 패턴에 대해 cors 허용 설정

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}