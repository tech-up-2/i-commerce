package com.example.i_commerce.domain.chat.config;

import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.TokenPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenUtil jwtTokenUtil;
    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);


        if(StompCommand.CONNECT == accessor.getCommand()){
            log.info("connect요청시 토큰 유효성 검증");
            String token = getValidToken(accessor);
            jwtTokenUtil.parseToken(token);
            log.info("토큰 검증 완료");
        }
        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            log.info("subscribe 검증");
            String token = getValidToken(accessor);
            TokenPayload payload = jwtTokenUtil.parseToken(token);
            //ws은 연결마다 세션이 하나씩 있으므로 거기에 memberId나 정보 등을 담아둘 수 있음...
            accessor.getSessionAttributes().put("memberId", payload.accountId());
            Long memberId = payload.accountId();
            String roomId = accessor.getDestination().split("/")[2];
            if(!chatService.isRoomParticipant(memberId, Long.parseLong(roomId))){
                throw new AppException(ChatErrorCode.NOT_A_ROOM_MEMBER);
            }
        }
        return message;
    }
    private String getValidToken(StompHeaderAccessor accessor){
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        if(bearerToken == null || !bearerToken.startsWith("Bearer ")){
            throw new AppException(ChatErrorCode.INVALID_STOMP_TOKEN_HEADER);
        }
        return bearerToken.substring(7);
    }

}
