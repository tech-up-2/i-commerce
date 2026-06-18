    package com.example.i_commerce.domain.chat.controller;

    import com.example.i_commerce.domain.chat.service.ChatService;
    import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendResponse;
    import com.example.i_commerce.domain.chat.service.dto.GroupChatListResponse;
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
    import org.springframework.web.bind.annotation.PatchMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    @Tag(name = "Chat API", description = "채팅 관련 API")
    @SecurityRequirement(name = "BearerAuth")
    @RestController
    @RequestMapping("/api/v1/chat")
    @RequiredArgsConstructor
    public class ChatController {

        private final ChatService chatService;

        //    채팅내역 조회
        @Operation(summary = "이전 채팅내역 조회", description = "채팅 내역을 조회힌다.")
        @GetMapping("/history/{roomId}")
        ApiResponse<List<ChatMessageSendResponse>> getChatHistory(@PathVariable Long roomId) {
            return chatService.getChatHistory(roomId);
        }

        //    읽은 채팅 조회하기
        @Operation(summary = "채팅 읽음 처리", description = "채팅방의 메시지를 읽음 처리한다.")
        @PostMapping("/room/{roomId}/read")
        public ApiResponse<Void> messageRead(@PathVariable Long roomId) {
            chatService.messageRead(roomId);
            return ApiResponse.success();
        }

        //    내 채팅방 목록 조회
        @Operation(summary = "내 채팅방 목록 조회", description = "내가 참여중인 채팅방 목록을 조회한다.")
        @GetMapping("/my/chat")
        public ApiResponse<List<MyChatListResponse>> getMyChatList() {
            return chatService.getMyChatList();
        }

        @Operation(summary = "개설된 그룹 채팅 목록 조회", description = "현재 개설된 그룹 채팅방 목록입니다.")
        @GetMapping("/group/list")
        public ApiResponse<List<GroupChatListResponse>> getGroupChatList() {
            return chatService.getGroupChatList();
        }

        @Operation(summary = "잘못 작성한 채팅을 제거", description = "이미 작성된 채팅을 채팅방 화면에서 제거합니다.")
        @PatchMapping("/messages/{messageId}/delete")
        public ApiResponse<Void> deleteMessage(@PathVariable Long messageId){
            chatService.deleteMessage(messageId);
            return ApiResponse.success();

        }

    }
