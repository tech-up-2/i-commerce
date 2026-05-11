package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendResponse;
import com.example.i_commerce.domain.chat.service.dto.MyChatListResponse;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//import com.example.i_commerce.domain.order.service.OrderService;

@Tag(name = "Chat API", description = "채팅 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    //    개인 1:1 채팅방 개설
//    JWT 도입시 파라미터 변경
    @Operation(summary = "개인 1:1 채팅방 개설", description = "개인 1:1 채팅방을 개설한다.")
    @PostMapping("/room/{otherMemberId}")
    public ApiResponse<Long> getOrCreatePrivateRoom(@PathVariable Long otherMemberId) {

        return chatService.getOrCreatePrivateRoom(otherMemberId);
    }

    //    단체 채팅방 개설
    @Operation(summary = "단체 채팅방 개설", description = "단체 채팅방을 개설한다.")
    @PostMapping("/group/{productId}")
    public ApiResponse<Long> CreateGroupRoom(@PathVariable Long productId) {
        return chatService.createGroupRoom(productId);
    }
    //    단체 채팅방 참여
    @Operation(summary = "단체 채팅방 참여", description = "단체 채팅방에 참여한다.")
    @PostMapping("/group/{roomId}/join")
    public ApiResponse<Void> joinGroupRoom(@PathVariable Long roomId) {
        chatService.joinGroupRoom(roomId);
        return ApiResponse.success();
    }


    //    단체 채팅방 나가기
    @DeleteMapping("/group/{roomId}/leave")
    public ApiResponse<Void> leaveGroupRoom(@PathVariable Long roomId) {
        chatService.leaveGroupRoom(roomId);
        return ApiResponse.success();
    }

    @GetMapping("/history/{roomId}")
    ApiResponse<List<ChatMessageSendResponse>> getChatHistory(@PathVariable Long roomId) {
        return chatService.getChatHistory(roomId);
    }

//    읽은 채팅 조회하기
    @PostMapping("/room/{roomId}/read")
    public ApiResponse<Void> messageRead(@PathVariable Long roomId) {
        chatService.messageRead(roomId);
        return ApiResponse.success();
    }
//    내 채팅방 목록 조회
    @GetMapping("/my/chat")
    public ApiResponse<List<MyChatListResponse>> getMyChatList(){
        return chatService.getMyChatList();
    }


}
