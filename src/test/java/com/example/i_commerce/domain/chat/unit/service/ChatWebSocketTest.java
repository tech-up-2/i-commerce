package com.example.i_commerce.domain.chat.unit.service;

import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendResponse;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.dto.TokenPayload;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ChatWebSocketTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    private BlockingQueue<ChatMessageSendResponse> subscribeResultBag;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    private Long testMemberId = 1L;
    private Long testRoomId;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        // SockJsClient 세팅
        // 서버가 .withSockJS()를 사용하므로 SockJsClient 필요
        // StandardWebSocketClient를 Transport로 감싸서 넘겨줌
        List<Transport> transports = List.of(
            new WebSocketTransport(new StandardWebSocketClient())
        );
        SockJsClient sockJsClient = new SockJsClient(transports);

        // STOMP 클라이언트 생성 및 JSON 컨버터 등록
        // JacksonJsonMessageConverter: Spring 7에서 MappingJackson2MessageConverter 대체
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        // 수신 메시지 담을 큐 초기화
        subscribeResultBag = new LinkedBlockingQueue<>();

        // 테스트용 JWT 토큰 생성
        // StompHandler의 CONNECT/SUBSCRIBE 검증을 통과하기 위해 필요
        TokenPayload payload = new TokenPayload(
            PrincipalType.MEMBER,
            testMemberId,
            MemberType.CUSTOMER,
            MemberStatus.ACTIVE,
            null
        );
        token = "Bearer " + jwtTokenUtil.createToken(payload);
    }
}
