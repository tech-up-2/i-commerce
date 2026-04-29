package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendRequest;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;


@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

/*    Stomp 방식의 MessageMapping 어노테이션을 활용
//클라이언트에서 특정 publish/roomId 형태로 메시지를 발행시 MessageMapping이 해당 메시지를 수신합니다.
DestinationVariable은 @MessageMapping 어노테이션으로 정의된 Websocket Controller 내에서만 사용한다.
기존 RequestParam, PathVariable과 같은 매핑 역할을 하는 어노테이션
 */
    @MessageMapping("/{roomId")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageSendRequest  request) {
        ChatMessageSendResponse response = chatService.saveMessage(roomId, request);
        messagingTemplate.convertAndSend("/topic/"+roomId, response);
    }
}

