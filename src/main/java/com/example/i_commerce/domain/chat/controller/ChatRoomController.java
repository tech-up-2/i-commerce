package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.service.ChatRoomService;
import com.example.i_commerce.domain.chat.service.ChatService;
import com.example.i_commerce.domain.chat.util.TempChatUtil;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat API Room", description = "채팅방 관련 API")
@SecurityRequirement(name = "BearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    //    개인 1:1 채팅방 개설
    //    JWT 도입시 파라미터 변경
    @Operation(summary = "개인 1:1 채팅방 개설", description = "개인 1:1 채팅방을 개설한다.")
    @PostMapping("/room/{otherMemberId}")
    public ApiResponse<Long> getOrCreatePrivateRoom(@PathVariable Long otherMemberId) {
        Long myId = TempChatUtil.getCurrentUserId();
        String roomKey = ChatRoom.generateRoomKey(myId, otherMemberId);
        try {
            return chatRoomService.getOrCreatePrivateRoom(otherMemberId);
        } catch (DataIntegrityViolationException e) {
            log.info("동시성 문제 발생");
            //서비스 트랜잭션이 롤백되므로 새 트랜잭션에서 db를 건들지 않고 먼저 생성이 된 ID를 가져온다.
            Long privateRoomId = chatRoomService.getPrivateRoomIdByRoomKey(roomKey);
            // 유저는 에러 화면 대신 새로운 방의 ID를 리턴받는다.
            return ApiResponse.success(privateRoomId);
        }
    }

    //    단체 채팅방 개설
    @Operation(summary = "단체 채팅방 개설", description = "단체 채팅방을 개설한다.")
    @PostMapping("/group/{productId}")
    public ApiResponse<Long> createGroupRoom(@PathVariable Long productId) {
        return chatRoomService.createGroupRoom(productId);
    }

    //    단체 채팅방 참여
    @Operation(summary = "단체 채팅방 참여", description = "단체 채팅방에 참여한다.")
    @PostMapping("/group/{roomId}/join")
    public ApiResponse<Void> joinGroupRoom(@PathVariable Long roomId) {
        chatRoomService.joinGroupRoom(roomId);
        return ApiResponse.success();
    }


    //    단체 채팅방 나가기
    @Operation(summary = "단체 채팅방 퇴장", description = "단체 채팅방에서 퇴장한다.")
    @DeleteMapping("/group/{roomId}/leave")
    public ApiResponse<Void> leaveGroupRoom(@PathVariable Long roomId) {
        chatRoomService.leaveGroupRoom(roomId);
        return ApiResponse.success();
    }
}
