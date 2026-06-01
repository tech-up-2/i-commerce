package com.example.i_commerce.domain.order.config;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@TestConfiguration
public class TestWebClientConfig {

    // 테스트용 가짜 서버의 URL을 동적으로 주입받아 WebClient를 빌드합니다.
    public static WebClient createTestWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(1)); // 테스트의 빠른 진행을 위해 타임아웃을 1초로 짧게 잡습니다.

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
