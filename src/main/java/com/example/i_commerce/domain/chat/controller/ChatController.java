package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
//    JWT 도입시 파라미터 변경
    @PostMapping("/room/create/{myId}/{otherMemberId}")
    public ApiResponse<Long> getOrCreatePrivateRoom(
        @PathVariable Long myId, @PathVariable Long otherMemberId) {

        return chatService.getOrCreatePrivateRoom(myId, otherMemberId);
    }

    //    단체 채팅방 개설
    @PostMapping("/group/{productId}/{myId}")
    public ApiResponse<Long> getOrCreateGroupRoom(
        @PathVariable Long productId, @PathVariable Long myId
    ) {
        return chatService.createGroupRoom(productId, myId);
    }

    //    단체 채팅방 참여
    @PostMapping("/group/{roomId}/join")
    public ApiResponse<Void> joinGroupRoom(@PathVariable Long roomId, @RequestParam Long myId) {
        chatService.joinGroupRoom(roomId, myId);
        return ApiResponse.success();
    }


    //    단체 채팅방 나가기
    @DeleteMapping("/group/{roomId}/leave")
    public ApiResponse<Void> leaveGroupRoom(@PathVariable Long roomId, @RequestParam Long myId) {
        chatService.leaveGroupRoom(roomId, myId);
        return ApiResponse.success();
    }

}
