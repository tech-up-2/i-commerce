package com.example.i_commerce.domain.chat.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.common.ChatIntegrationTestSupport;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import com.fasterxml.jackson.databind.ObjectMapper;

class ChatSubscribeIntegrationTest extends ChatIntegrationTestSupport {


    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private ChatTestSet testSet;
    private StompHeaders connectHeaders;
    private String token;

    protected WebSocketStompClient createStompClient(){
        WebSocketStompClient stompClient = new WebSocketStompClient(
            new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
        );

        stompClient.setMessageConverter(
            new JacksonJsonMessageConverter()
        );
        return stompClient;
    }
    @BeforeEach
    void setUp(){
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
}
