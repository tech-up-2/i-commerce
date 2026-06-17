package com.example.i_commerce.domain.order.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.internal.shaded.reactor.pool.PoolAcquireTimeoutException;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${toss.baseUrl:https://api.tosspayments.com/v1/payments}")
    private String tossBaseUrl;

    @Bean
    public WebClient tossWebClient() {


        log.info("=================================================");
        log.info("[WebClient 주입 확인] Toss Base URL: {}", tossBaseUrl);
        log.info("=================================================");

        ConnectionProvider connectionProvider = ConnectionProvider.builder("tossConnectionPool")
                .maxConnections(100) // 최대 연결 수
                .pendingAcquireMaxCount(1000) // 대기 중인 요청 최대 수
                .pendingAcquireTimeout(java.time.Duration.ofSeconds(10)) // 대기 시간 초과 설정
                .maxIdleTime(Duration.ofSeconds(20))          // 유휴 커넥션 유지 시간
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 연결 타임아웃 설정
                .responseTimeout(java.time.Duration.ofSeconds(60)) // 응답 타임아웃
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS)));


        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(tossBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();

    }


}
