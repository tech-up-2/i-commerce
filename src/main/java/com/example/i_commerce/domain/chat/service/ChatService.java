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
import com.example.i_commerce.domain.chat.service.dto.MyChatListResponse;
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
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ChatStatusRepository chatStatusRepository;
    private final DataEncryptor dataEncryptor;


    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
            .chatRoom(chatRoom)
            .member(member)
            .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public ApiResponse<Long> getOrCreatePrivateRoom(Long otherMemberId) {
// 1. 시큐리티 로직 완성 후 주석 해제
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(
                MemberErrorCode.USER_NOT_FOUND));
        Member otherMember = memberRepository.findById(otherMemberId)
            .orElseThrow(() -> new AppException(
                MemberErrorCode.USER_NOT_FOUND));
//       나와 상대방이 1:1 채팅을 이미 참여하고 있다면 에러코드를 return
//       사용자 입장에서는 에러코드 보다는 참여하고있는 채팅 리다이렉션이 훨씬 편리할 것 같음.
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(
            member.getId(), otherMember.getId());
        if (chatRoom.isPresent()) {
            throw new AppException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }
//      만약에 1:1 채팅방이 없을 경우 기존 채팅방 개설
        String userName = dataEncryptor.decrypt(member.getName());
        String otherUserName = dataEncryptor.decrypt(otherMember.getName());
        ChatRoom newRoom = ChatRoom.builder()
            .isGroupChat(false)
            .name(userName + "님이 요청한" + otherUserName + "님과의 채팅")
            .build();
        chatRoomRepository.save(newRoom);
//        두 사람을 채팅방에 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return ApiResponse.success(newRoom.getId());
    }

    public ApiResponse<Long> createGroupRoom(Long productId) {
//      채팅방 참여 멤버 검증
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(
                MemberErrorCode.USER_NOT_FOUND));
//      상품 존재여부 검증
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(
            ProductErrorCode.PRODUCT_NOT_FOUND));

//      채팅방 중복 여부 검증
        if (chatRoomRepository.existsByProductIdAndIsGroupChat(productId, true)) {
            throw new AppException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

//      채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
            .name(product.getName() + "상품 채팅방")
            .isGroupChat(true)
            .product(product)
            .build();

        chatRoomRepository.save(chatRoom);

//      최초 생성자를 방에 참가시킨다.
        addParticipantToRoom(chatRoom, member);

        return ApiResponse.success(chatRoom.getId());
    }


    public ApiResponse<Void> joinGroupRoom(Long roomId) {
        //실제로 채팅방이 존재하는지 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        //해당 채팅방이 그룹채팅인지 검증
        if (!chatRoom.getIsGroupChat()) {
            throw new AppException(ChatErrorCode.CANNOT_JOIN_PRIVATE_ROOM);
        }
        //참여하고자 하는 멤버가 존재하는지 검증
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
        //강퇴 여부 확인(미구현)

        //이미 참여하고 있는지 검증 및 멤버 추가
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(
            chatRoom, member);
        if (participant.isPresent()) {
            throw new AppException(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }
        addParticipantToRoom(chatRoom, member);
        return ApiResponse.success();
    }

    public ApiResponse<Void> leaveGroupRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
        //해당 채팅방이 그룹채팅인지 검증
        if (!chatRoom.getIsGroupChat()) {
            throw new AppException(ChatErrorCode.ONLY_GROUP_CHAT_CAN_LEAVE);
        }
        //해당 사용자가 해당 채팅방에 참가하고 있는지 검증
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndMember(
                chatRoom, member)
            .orElseThrow(() -> new AppException(ChatErrorCode.NOT_A_ROOM_MEMBER));
        //유저를 해당 채팅방에서 제거
        chatParticipantRepository.delete(chatParticipant);

        //해당 방에 유저가 아무도 없는지 조회
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom(chatRoom);
        //만약 유저가 한명도 없으면 채팅방을 제거한다.
        if (participants.isEmpty()) {
            //소프트 딜리트 적용
            chatRoom.delete();
            //실제로 DB에서 제거가 되지 않으므로 저장 해주어야함.
            chatRoomRepository.save(chatRoom);
        }

        return ApiResponse.success();
    }

    public Boolean isRoomParticipant(Long myId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Member member = memberRepository.findById(myId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        return chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).isPresent();
    }

    public ApiResponse<Void> messageRead(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
//      기존 강의에서는 읽은 채팅과 안읽은 채팅을 모두 불러왔지만 안읽은 채팅만 불러오는 것이 조회 측면에서 효율적임
        List<MessageReadStatus> readStatuses = chatStatusRepository.findByChatRoomAndMemberAndIsReadFalse(
            chatRoom, member);
        for (MessageReadStatus r : readStatuses) {
            r.updateIsRead(true);
        }
        return ApiResponse.success();
    }

    public ApiResponse<List<MyChatListResponse>> getMyChatList() {
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
        List<ChatParticipant> participants = chatParticipantRepository.findAllByMember(member);
        List<MyChatListResponse> myChatListResponses = new ArrayList<>();
        log.info("participants size: {}", participants.size());
        for (ChatParticipant p : participants) {
//            find와 같이 JPA에는 count라는 네이밍 규칙이 존재 Long 형태로 반환해줌
            Long count = chatStatusRepository.countByChatRoomAndMemberAndIsReadFalse(
                p.getChatRoom(), member);
            MyChatListResponse responseDto = MyChatListResponse.builder()
                .roomId(p.getChatRoom().getId())
                .roomName(p.getChatRoom().getName())
                .isGroupChat(p.getChatRoom().getIsGroupChat())
                .unReadCount(count)
                .build();
            myChatListResponses.add(responseDto);
            log.info(myChatListResponses.toString());
        }
        return ApiResponse.success(myChatListResponses);
    }

    public ApiResponse<Void> saveMessage(Long roomId, ChatMessageSendRequest chatMessageSendRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Member member = memberRepository.findById(chatMessageSendRequest.senderId())
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
        ChatMessage chatMessage = ChatMessage.builder()
            .chatRoom(chatRoom)
            .member(member)
            .content(chatMessageSendRequest.message())
            .build();
        chatMessageRepository.save(chatMessage);
        return ApiResponse.success();
    }

    public ApiResponse<List<ChatMessageSendResponse>> getChatHistory(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new AppException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        Member member = memberRepository.findById(TempChatUtil.getCurrentUserId()).orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        for (ChatParticipant p : chatParticipants) {
            if (p.getMember().equals(member)) {
                check = true;
            }
        }
        if (!check) {
            new AppException(ChatErrorCode.NOT_A_ROOM_MEMBER);
        }
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        List<ChatMessageSendResponse> chatMessageSendResponses = new ArrayList<>();
        for (ChatMessage messages : chatMessages) {
            ChatMessageSendResponse messagesDto = ChatMessageSendResponse.builder()
                .message(messages.getContent())
                .messageId(messages.getId())
                .senderId(messages.getMember().getId())
                .build();
            chatMessageSendResponses.add(messagesDto);
        }
        return ApiResponse.success(chatMessageSendResponses);
    }
}