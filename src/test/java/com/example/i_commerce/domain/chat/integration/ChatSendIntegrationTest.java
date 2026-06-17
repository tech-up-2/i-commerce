package com.example.i_commerce.domain.chat.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.common.ChatIntegrationTestSupport;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendRequest;
import com.example.i_commerce.domain.member.entity.Member;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import java.lang.reflect.Type;

public class ChatSendIntegrationTest extends ChatIntegrationTestSupport {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private ChatTestSet testSet;
    private StompSession customerSession;
    private StompSession sellerSession;


    protected WebSocketStompClient createStompClient() {

        WebSocketStompClient stompClient =
            new WebSocketStompClient(
                new SockJsClient(
                    List.of(
                        new WebSocketTransport(
                            new StandardWebSocketClient()
                        )
                    )
                )
            );

        stompClient.setMessageConverter(
            new JacksonJsonMessageConverter()
        );

        return stompClient;
    }

    private StompSession connect(String token) throws Exception {

        StompHeaders headers = new StompHeaders();

        headers.add(
            "Authorization",
            "Bearer " + token
        );

        return stompClient.connectAsync(
            "ws://localhost:" + port + "/connect",
            new WebSocketHttpHeaders(),
            headers,
            new StompSessionHandlerAdapter() {
            }
        ).get(5, TimeUnit.SECONDS);
    }

    @BeforeEach
    void setUp() throws Exception {

        stompClient = createStompClient();

        testSet = saveDefaultChatTestSet();

        customerSession = connect(
            createMemberToken(
                testSet.customer()
            )
        );

        sellerSession = connect(
            createSellerToken(
                testSet.sellerMember()
            )
        );
    }

    @Test
    @DisplayName("[성공]: 메시지 전송 시 상대방이 수신하고 DB에 저장할 수 있습니다.")
    void send_Success() throws Exception {

        BlockingQueue<ChatMessageSendRequest> messages =
            new LinkedBlockingQueue<>();
        sellerSession.subscribe(
            "/topic/" + testSet.room().getId(),
            new StompFrameHandler() {

                @Override
                public Type getPayloadType(
                    StompHeaders headers
                ) {
                    return ChatMessageSendRequest.class;
                }

                @Override
                public void handleFrame(
                    StompHeaders headers,
                    Object payload
                ) {

                    messages.add(
                        (ChatMessageSendRequest) payload
                    );
                }
            }
        );

        ChatMessageSendRequest request =
            new ChatMessageSendRequest(
                "안녕하세요",
                testSet.customer().getId()
            );
        customerSession.send(
            "/publish/" + testSet.room().getId(),
            request
        );

        ChatMessageSendRequest received =
            messages.poll(5, TimeUnit.SECONDS); // 메시지가 들어오는 것을 기다리는 것
        //5초 동안 메시지가 들어오는 걸 기다리다가 안들어오면 Null을 반환

        assertThat(received).isNotNull();// 상대방이 수신 헀다.
        assertThat(received.message()).isEqualTo("안녕하세요");// 메시지 내용이 올바른가.
        assertThat(received.senderId()).isEqualTo(testSet.customer().getId());//보낸 사람이 일치하는가
        assertThat(chatMessageRepository.count()).isEqualTo(1);

    }

    @Test
    @DisplayName("[예외]: 존재하지 않는 방 ID로 메시질르 전송하면, 메시지가 전달되지 않고 DB에 저장되지 않습니다.")
    void send_Fail_ChatRoomNotFound() throws Exception {
        Long invalidRoomId = 99999L;
        BlockingQueue<ChatMessageSendRequest> messages =
            new LinkedBlockingQueue<>();

        sellerSession.subscribe(
            "/topic/" + testSet.room().getId(),
            new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return ChatMessageSendRequest.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    messages.add((ChatMessageSendRequest) payload);
                }
            }
        );
        ChatMessageSendRequest request = new ChatMessageSendRequest(
            "존재하지 않는 테스트방에 전송된 메시지",
            testSet.customer().getId()
        );
        customerSession.send(
            "/publish/" + invalidRoomId,
            request
        );
        ChatMessageSendRequest received =
            messages.poll(5, TimeUnit.SECONDS); // 메시지가 들어오는 것을 기다리는 것
        //5초 동안 메시지가 들어오는 걸 기다리다가 안들어오면 Null을 반환
        assertThat(received).isNull();
        assertThat(chatMessageRepository.count()).isEqualTo(0);

    }
    @Test
    @DisplayName("[예외] 빈 메시지가 전송될 시 전송과 저장이 되지 않습니다.")
    void send_Fail_NullMessage() throws Exception {
        BlockingQueue<ChatMessageSendRequest> messages =
            new LinkedBlockingQueue<>();
        sellerSession.subscribe(
            "/topic/" + testSet.room().getId(),
            new StompFrameHandler() {

                @Override
                public Type getPayloadType(
                    StompHeaders headers
                ) {
                    return ChatMessageSendRequest.class;
                }

                @Override
                public void handleFrame(
                    StompHeaders headers,
                    Object payload
                ) {

                    messages.add(
                        (ChatMessageSendRequest) payload
                    );
                }
            }
        );

        ChatMessageSendRequest request =
            new ChatMessageSendRequest(
                null,
                testSet.customer().getId()
            );
        customerSession.send(
            "/publish/" + testSet.room().getId(),
            request
        );

        ChatMessageSendRequest received =
            messages.poll(5, TimeUnit.SECONDS); // 메시지가 들어오는 것을 기다리는 것
        //5초 동안 메시지가 들어오는 걸 기다리다가 안들어오면 Null을 반환
        assertThat(received).isNull();
        assertThat(chatMessageRepository.count()).isEqualTo(0);
    }
//    @Test
//    @DisplayName("[에외]: 채팅에 참여하지 않은 인원이 메시지를 전송하면, 방 참여자는 이를 수신할 수 없습니다. 추가적으로 DB에 저장되지 않습니다.")
//    void send_Fail_NotChatRoomMember() throws Exception {
//        Member stranger = saveMember("stranger");
//        String strangerToken = createMemberToken(stranger);
//        StompSession strangerSession = connect(strangerToken);
//        BlockingQueue<ChatMessageSendRequest> messages =
//            new LinkedBlockingQueue<>();
//        sellerSession.subscribe(
//            "/topic/" + testSet.room().getId(),
//            new StompFrameHandler() {
//                @Override
//                public Type getPayloadType(StompHeaders headers) {
//                    return ChatMessageSendRequest.class;
//                }
//
//                @Override
//                public void handleFrame(StompHeaders headers, @Nullable Object payload) {
//                    messages.add((ChatMessageSendRequest) payload);
//                }
//            }
//        );
//        ChatMessageSendRequest request = new ChatMessageSendRequest(
//            "채팅방 참여하지 않은 유저의 메시지",
//            stranger.getId()
//        );
//        strangerSession.send(
//            "/publish/"+ testSet.room().getId(),
//            request
//        );
//        ChatMessageSendRequest received =
//            messages.poll(5, TimeUnit.SECONDS); // 메시지가 들어오는 것을 기다리는 것
//        //5초 동안 메시지가 들어오는 걸 기다리다가 안들어오면 Null을 반환
//        assertThat(received).isNull();
//        assertThat(chatMessageRepository.count()).isEqualTo(0);
//    }
}
