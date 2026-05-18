package com.example.i_commerce.domain.chat.service;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.MessageReadStatus;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.repository.ChatStatusRepository;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendRequest;
import com.example.i_commerce.domain.chat.service.dto.ChatMessageSendResponse;
import com.example.i_commerce.domain.chat.service.dto.GroupChatListResponse;
import com.example.i_commerce.domain.chat.service.dto.MyChatListResponse;
import com.example.i_commerce.domain.chat.util.ChatRoleChecker;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;
import com.example.i_commerce.domain.chat.util.TempChatUtil;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;

import java.lang.classfile.instruction.SwitchCase;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberRepository memberRepository;
    private final ChatStatusRepository chatStatusRepository;


    public ApiResponse<Void> messageRead(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
//      기존 강의에서는 읽은 채팅과 안읽은 채팅을 모두 불러왔지만 안읽은 채팅만 불러오는 것이 조회 측면에서 효율적임
        List<MessageReadStatus> readStatuses = chatStatusRepository.findByChatRoomAndMemberIdAndIsReadFalse(
            chatRoom, member.getId());
        for (MessageReadStatus r : readStatuses) {
            r.updateIsRead(true);
        }
        return ApiResponse.success();
    }

    public ApiResponse<List<MyChatListResponse>> getMyChatList() {
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
        List<MyChatListResponse> myChatListResponses = chatStatusRepository.findMyChatList(
            member.getId());
        return ApiResponse.success(myChatListResponses);
    }

    public ApiResponse<List<GroupChatListResponse>> getGroupChatList() {
        List<ChatRoom> groupRooms = chatRoomRepository.findByIsGroupChatTrueAndDeletedAtIsNull();
        List<GroupChatListResponse> reponseList = new ArrayList<>();
        for (ChatRoom room : groupRooms) {
            GroupChatListResponse dto = new GroupChatListResponse(room.getId(), room.getName());
            reponseList.add(dto);
        }
        return ApiResponse.success(reponseList);
    }


    //    해당 부분은 메시지를 보냈을 때, 새로고침을 통해만 확인이 가능하기 때문에 ws를 이용해서 실시간 처리가 가능하도록 하는게 최종 목표.
    public ApiResponse<Void> saveMessage(Long roomId,
        ChatMessageSendRequest chatMessageSendRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
//            멤버에서 ID를 가져오는 부분 시큐리티가 ws에서 작동하려면 StompHandler 수정이 필요
//            추후 리펙토링을 통해 보완예정
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Long memberId = TempChatUtil.getCurrentUserId();
        ChatMessage chatMessage = ChatMessage.builder()
            .chatRoom(chatRoom)
            .memberId(memberId)
            .content(chatMessageSendRequest.message())
            .build();
        chatMessageRepository.save(chatMessage);
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant p : chatParticipants) {
            MessageReadStatus readStatus = MessageReadStatus.builder()
                .chatRoom(chatRoom)
                .memberId(p.getMemberId())
                .chatMessage(chatMessage)
                .isRead(p.getMemberId().equals(memberId))
                .build();
            chatStatusRepository.save(readStatus);
        }
        return ApiResponse.success();
    }

    public ApiResponse<List<ChatMessageSendResponse>> getChatHistory(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        for (ChatParticipant p : chatParticipants) {
            if (p.getMemberId().equals(member.getId())) {
                check = true;
            }
        }
        if (!check) {
            throw new AppException(ChatErrorCode.NOT_A_ROOM_MEMBER);
        }
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(
            chatRoom);
        List<ChatMessageSendResponse> chatMessageSendResponses = new ArrayList<>();
        for (ChatMessage messages : chatMessages) {
            ChatMessageSendResponse messagesDto = ChatMessageSendResponse.builder()
                .message(messages.isBlind() ? "관리자에 의해 가려진 메시지입니다" : messages.getContent())
                .messageId(messages.getId())
                .senderId(messages.getMemberId())
                .build();
            chatMessageSendResponses.add(messagesDto);
        }
        return ApiResponse.success(chatMessageSendResponses);
    }

    public Boolean isRoomParticipant(Long myId, Long roomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        return chatParticipantRepository.findByChatRoomAndMemberId(chatRoom, myId).isPresent();

    }
}