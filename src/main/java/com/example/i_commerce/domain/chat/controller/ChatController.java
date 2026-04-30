package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
//    개인 1:1 채팅방 개설
//    JWT 도입시 수정해야하는 내용: 파라미터 어노테이션에서 myId 제거와 ReqParam으로 어노테이션 변경
    @PostMapping("/room/create/{myId}/{otherMemberId}")
    public ApiResponse<Long> getOrCreatePrivateRoom(
        @PathVariable Long myId, @PathVariable Long otherMemberId){

        return chatService.getOrCreatePrivateRoom(myId, otherMemberId);
    }
//    단체 채팅방 개설
    @PostMapping("/group/{productId}/{myId}")
    public ApiResponse<Long> getOrCreateGroupRoom(
        @PathVariable Long productId, @PathVariable Long myId
    ){
        return chatService.createGroupRoom(productId, myId);
    }

}
