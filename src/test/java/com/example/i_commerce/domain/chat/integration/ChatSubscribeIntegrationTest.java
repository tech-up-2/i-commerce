package com.example.i_commerce.domain.chat.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.i_commerce.common.ChatIntegrationTestSupport;
import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
class ChatSubscribeIntegrationTest extends ChatIntegrationTestSupport {


    @LocalServerPort
    private int port;
    @Autowired
    private ChatService chatService;

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
    @DisplayName("[성공]: 성공적으로 채팅방에 Subscribe를 할 수 있습니다.")
    void subscribe_Success() throws Exception {
        StompSession session = stompClient.connectAsync(
            "ws://localhost:" + port + "/connect",
            new WebSocketHttpHeaders(),
            connectHeaders,
            new StompSessionHandlerAdapter() {
            }
        ).get(5, TimeUnit.SECONDS);

        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination("/topic/" + testSet.room().getId());

        session.subscribe(
            subscribeHeaders,
            new StompSessionHandlerAdapter() {
            }
        );
        assertThat(session.isConnected()).isTrue();
    }

    @Test
    @DisplayName("[예외]: 채팅방에 접속하지 않은 유저가 Sub 요청시 예외가 발생합니다..")
    void subscribe_Fail_Invalid_SubScribe() throws Exception {
        Member stranger = saveMember("stranger");//채팅방에 참여중이지 않은 유저를 생성해줍니다.

        String strangerToken = createMemberToken(stranger);// 해당 유저의 토큰을 발급합니다.

        StompHeaders strangerHeaders = new StompHeaders();
        strangerHeaders.add("Authorization", "Bearer " + strangerToken);
        StompSession session = stompClient.connectAsync(
            "ws://localhost:" + port + "/connect",
            new WebSocketHttpHeaders(),
            strangerHeaders,
            new StompSessionHandlerAdapter() {
            }

        ).get(5, TimeUnit.SECONDS);
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination("/topic/" + testSet.room().getId());
//        log.info("Customer = {}", testSet.customer().getId());
//        log.info("Seller = {}", testSet.sellerMember().getId());
//        log.info("Stranger = {}", stranger.getId());
        session.subscribe(
            subscribeHeaders,
            new StompSessionHandlerAdapter() {}
        );

//        비동기 요청으로 sub 처리되므로 서버측 검증이 완료될 때까지 대기
        Thread.sleep(1000);


        assertThat(
            chatService.isRoomParticipant(
                stranger.getId(),
                testSet.room().getId()
            )
        ).isFalse();
    }
    @Test
    @DisplayName("[예외]: 존재하지 않는 채팅방 Subscribe 요청 시 연결은 유지된다.")
    void subscribe_Fail_NotFound_Room() throws Exception {

        StompSession session = stompClient.connectAsync(
            "ws://localhost:" + port + "/connect",
            new WebSocketHttpHeaders(),
            connectHeaders,
            new StompSessionHandlerAdapter() {}
        ).get(5, TimeUnit.SECONDS);
        StompHeaders subscribeHeaders = new StompHeaders();
        subscribeHeaders.setDestination("/topic/9999");

        session.subscribe(
            subscribeHeaders,
            new StompSessionHandlerAdapter() {}
        );
        assertThat(session.isConnected()).isTrue();


    }

}
