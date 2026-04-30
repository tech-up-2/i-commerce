package com.example.i_commerce.domain.chat.service;

import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
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

    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
            .chatRoom(chatRoom)
            .member(member)
            .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public ApiResponse<Long> getOrCreatePrivateRoom(Long myId, Long otherMemberId) {
// 1. 시큐리티 로직 완성 후 주석 해제
        // Long myId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Member member = memberRepository.findById(myId).orElseThrow(() -> new AppException(
            ErrorCode.USER_NOT_FOUND));
        Member otherMember =  memberRepository.findById(otherMemberId).orElseThrow(() -> new AppException(
            ErrorCode.USER_NOT_FOUND));
//       나와 상대방이 1:1 채팅을 이미 참여하고 있다면 해당 roomId를 return
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(member.getId(), otherMember.getId());
        if (chatRoom.isPresent()) {
            throw new AppException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }
//      만약에 1:1 채팅방이 없을 경우 기존 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
            .isGroupChat(false)
            .name(member.getName()+"님이 요청한"+otherMember.getName()+"님과의 채팅")
            .build();
        chatRoomRepository.save(newRoom);
//        두 사람을 채팅방에 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return ApiResponse.success(newRoom.getId());
    }
    public ApiResponse<Long> createGroupRoom(Long productId, Long myId){
//      채팅방 참여 멤버 검증
        Member member = memberRepository.findById(myId).orElseThrow(() -> new AppException(
            ErrorCode.USER_NOT_FOUND));
//      상품 존재여부 검증
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

//      채팅방 중복 여부 검증
        if(chatRoomRepository.existsByProductIdAndIsGroupChat(productId, false)) {
            throw new AppException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

//      채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
            .name(product.getName()+"상품 채팅방")
            .isGroupChat(true)
            .product(product)
            .build();

        chatRoomRepository.save(chatRoom);

//      최초 생성자를 방에 참가시킨다.
        addParticipantToRoom(chatRoom, member);

        return ApiResponse.success(chatRoom.getId());
    }
}
