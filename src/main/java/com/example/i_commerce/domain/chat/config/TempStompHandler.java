package com.example.i_commerce.domain.chat.config;

import com.example.i_commerce.domain.chat.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@RequiredArgsConstructor
public class TempStompHandler implements ChannelInterceptor {

    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT == accessor.getCommand()){
            System.out.println("connect 요청");
        }
        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            log.info("subscribe 검증");
            String memberId = accessor.getFirstNativeHeader("memberId");
            String roomId = accessor.getDestination().split("/")[2];
            if(!chatService.isRoomParticipant(Long.parseLong(memberId), Long.parseLong(roomId))){
                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
            }
        }
        return message;
    }
}