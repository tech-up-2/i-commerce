package com.example.i_commerce.domain.chat.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.i_commerce.common.ChatIntegrationTestSupport;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

class ChatConnectIntegrationTest extends ChatIntegrationTestSupport {


    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private ChatTestSet testSet;
    private StompHeaders connectHeaders;
    private String token;

    protected WebSocketStompClient createStompClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(
            new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
        );

        stompClient.setMessageConverter(
            new JacksonJsonMessageConverter()
        );
        return stompClient;
    }

    @BeforeEach
    void setUp() {
        stompClient = createStompClient();

        testSet = saveDefaultChatTestSet();

        token = createMemberToken(testSet.customer());

        connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

    }

    @Test
    @DisplayName("시나리오 1 [성공]: 유요한 JWT로 CONNECT 요청시 연결됩니다.")
    void connect_Success() throws Exception {

        StompSession session = stompClient.connectAsync(
            "ws://localhost:" + port + "/connect",
            new WebSocketHttpHeaders(),
            connectHeaders,
            new StompSessionHandlerAdapter() {
            }
        ).get(5, TimeUnit.SECONDS);
        assertThat(session).isNotNull();
        assertThat(session.isConnected()).isTrue();
    }

    @Test
    @DisplayName("시나리오 2 [예외]: Authorization 헤더가 없는 상태에서 요청시 접근을 거부합니다.")
//    하단 에러 검증에서 AppException을 사용해도 정상적으로 테스트를 통과할 수 없었음 비동기 방식의
    void connect_Fail_Authorization_Header_Empty() throws Exception {
        StompHeaders invalidHeaders = new StompHeaders();// 기존 헤더에는 토큰 정보가 들어있지만 비어있다고 가정
        assertThatThrownBy(() ->
            stompClient.connectAsync(
                "ws://localhost:" + port + "/connect",
                new WebSocketHttpHeaders(),
                invalidHeaders,
                new StompSessionHandlerAdapter() {
                }
            ).get(5, TimeUnit.SECONDS)
        ).isInstanceOf(ExecutionException.class);
    }

    @Test
    @DisplayName("시나리오 3 [예외]: 유효하지 않은 JWT 토큰으로 연결을 시도시 접근을 거부합니다.")
    void connect_Fail_Invalid_Token() throws Exception {
        StompHeaders invalidHeaders = new StompHeaders();
        invalidHeaders.add("Authorization", "Bearer invalid.jwt.token");

        assertThatThrownBy(() ->
            stompClient.connectAsync(
                "ws://localhost:" + port + "/connect",
                new WebSocketHttpHeaders(),
                invalidHeaders,
                new StompSessionHandlerAdapter() {
                }

            ).get(5, TimeUnit.SECONDS)
        ).isInstanceOf(ExecutionException.class);
    }

}
