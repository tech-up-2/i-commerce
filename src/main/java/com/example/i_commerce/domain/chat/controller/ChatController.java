package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
//  개인 1:1 채팅방 개설
    @PostMapping("/room/create")
    public ApiResponse<Long> getOrCreatePrivateRoom(
        @RequestParam Long myId, @RequestParam Long otherMemberId){

        return chatService.getOrCreatePrivateRoom(myId, otherMemberId);
    }
//    단체 채팅방 개설




}
