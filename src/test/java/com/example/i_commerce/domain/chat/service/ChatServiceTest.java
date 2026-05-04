package com.example.i_commerce.domain.chat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatParticipantRepository chatParticipantRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProductRepository productRepository;

    private Member member;
    private Member otherMember;
    private ChatRoom singlechatRoom;
    private ChatRoom groupchatRoom;
    private Product product;
    private ChatParticipant chatParticipant;
    private Category category;


    @BeforeEach
    public void MemberInit() {
        // 카테고리 생성
        category = Category.builder()
            .id(1L)
            .name("테스트 카테고리")
            .depth(1)
            .build();

        // 상품 생성
        product = Product.builder()
            .id(1L)
            .storeId(1L)
            .category(category)
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .optionType(1)
            .status("ON_SALE")
            .build();

        member = Member.builder()
            .id(1L)
            .email("user1@test.com")
            .password("1234")
            .name("테스트 유저1")
            .sex(Gender.MALE)
            .birthday("20050505")
            .phoneNumber("01011111111")
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.CUSTOMER)
            .build();
        otherMember = Member.builder()
            .id(2L)
            .email("user2@test.com")
            .password("4321")
            .name("테스트 유저2")
            .sex(Gender.MALE)
            .birthday("20060606")
            .phoneNumber("01022222222")
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.SELLER)
            .build();

        singlechatRoom = ChatRoom.builder()
            .id(5L)
            .isGroupChat(false)
            .name("테스트 1:1채팅")
            .build();
        groupchatRoom = ChatRoom.builder()
            .id(6L)
            .isGroupChat(true)
            .name("테스트 그룹채팅")
            .build();

        chatParticipant = ChatParticipant.builder()
            .id(1L)
            .chatRoom(groupchatRoom)
            .member(member)
            .isBan(false)
            .build();

    }


    @Test
    @DisplayName("1:1 채팅방이 이미 존재하면 예외를 터트립니다.")
    void existingChatRoom() {

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(memberRepository.findById(otherMember.getId())).thenReturn(Optional.of(otherMember));
        when(chatParticipantRepository.findExistingPrivateRoom(member.getId(),
            otherMember.getId())).thenReturn(Optional.of(singlechatRoom));

        AppException exception = assertThrows(AppException.class,
            () -> chatService.getOrCreatePrivateRoom(
                member.getId(), otherMember.getId()));

        Assertions.assertThat(exception.getErrorCode())
            .isEqualTo(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("그룹 채팅방이 이미 존재하면 예외를 터트립니다.")
    void createGroupChatRoom() {

        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(chatRoomRepository.existsByProductIdAndIsGroupChat(product.getId(), true)).thenReturn(
            true);

        AppException exception = assertThrows(AppException.class, () ->
            chatService.createGroupRoom(product.getId(), member.getId()));

        Assertions.assertThat(exception.getErrorCode())
            .isEqualTo(ChatErrorCode.CHAT_ROOM_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("그룹 채팅방이 없으면 새로 생성합니다.")
    void createGroupRoomSuccess() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(chatRoomRepository.existsByProductIdAndIsGroupChat(product.getId(), true)).thenReturn(
            false);

        ApiResponse<Long> response = chatService.createGroupRoom(product.getId(), member.getId());

        assertEquals("SUCCESS", response.code());
    }


    @Test
    @DisplayName("그룹 채팅방에 참여합니다.")
    void joinGroupChatRoom() {
        //when
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(chatRoomRepository.findById(groupchatRoom.getId())).thenReturn(
            Optional.of(groupchatRoom));

        ApiResponse<Void> response = chatService.joinGroupRoom(groupchatRoom.getId(),
            member.getId());

        assertEquals("SUCCESS", response.code());
    }

    @Test
    @DisplayName("그룹 채팅방에서 퇴장합니다.")
    void leaveGroupRoom() {
        when(chatRoomRepository.findById(groupchatRoom.getId())).thenReturn(
            Optional.of(groupchatRoom));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(chatParticipantRepository.findByChatRoomAndMember(groupchatRoom, member))
            .thenReturn(Optional.of(chatParticipant));

        ApiResponse<Void> response = chatService.leaveGroupRoom(groupchatRoom.getId(),
            member.getId());

        assertEquals("SUCCESS", response.code());

    }
}

