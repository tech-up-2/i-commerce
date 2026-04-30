package com.example.i_commerce.global.config;

import com.example.i_commerce.global.auth.JwtAuthFilter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class Securityconfigs {

    private final JwtAuthFilter jwtAuthFilter;

    public Securityconfigs(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain myFilter(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)//csrf(공격)에 대해서 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)//HTTP Basic 비활성화
            //특정 url 패턴에 대해서는 Authentication 객체 요구하지 않음. (인증처리 제외)
            .authorizeHttpRequests(
                a -> a.requestMatchers("/member/create", "/member/doLogin", "/connect/**", "/api/v1/chat/**").permitAll().anyRequest()
                    .authenticated())
            .sessionManagement(
                s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//세션 방식을 사용하지 않겠다라는 의미
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("*"));//모든 HTTP 메서드 접근요청 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));//모든 헤더 값을 허용하겠다.
        configuration.setAllowCredentials(true);//자격증명을 허용하겠다.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);//모든 url에 패턴에 대해 cors 허용 설정
        return source;

    }

    @Bean
    public PasswordEncoder makePassword(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
