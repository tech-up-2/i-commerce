package com.example.i_commerce.domain.chat.config;

import com.example.i_commerce.domain.chat.service.ChatService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.secretKey}")
    private String secretKey;
    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

//        시큐리티 완성시 주석해제 후 사용
//        if(StompCommand.CONNECT == accessor.getCommand()){
//            System.out.println("connect요청시 토큰 유효성 검증");
//            String bearerToken = accessor.getFirstNativeHeader("Authorization");
//            String token = bearerToken.substring(7);
//            Jwts.parserBuilder()//payload qnqns
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//            System.out.println("토큰 검증 완료");
//        }
//        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
//            log.info("subscribe 검증");
//            String bearerToken = accessor.getFirstNativeHeader("Authorization");
//            String token = bearerToken.substring(7);
//            Claims claims = Jwts.parserBuilder()//payload qnqns
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//            String email = claims.getSubject();
//            String roomId = accessor.getDestination().split("/")[2];
//            if(!chatService.isRoomParticipant(email, Long.parseLong(roomId))){
//                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
//            }
//        }
        return message;
    }

}
