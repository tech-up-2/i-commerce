package com.example.i_commerce.domain.chat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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

    @Test
    @DisplayName("1:1 채팅이 존재한다면 에러코드를 반환하는 테스트")
    void existingChatRoom() {
//    Given
        Member member = Member.builder()
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
        Member otherMember =  Member.builder()
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


        ChatRoom chatRoom = ChatRoom.builder()
            .id(5L)
            .isGroupChat(false)
            .name("테스트 채팅")
            .build();
        // When
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(memberRepository.findById(otherMember.getId())).thenReturn(Optional.of(otherMember));
        when(chatParticipantRepository.findExistingPrivateRoom(member.getId(), otherMember.getId())).thenReturn(Optional.of(chatRoom));

        // Then
        AppException exception = assertThrows(AppException.class, () -> chatService.getOrCreatePrivateRoom(
            member.getId(),  otherMember.getId()));

        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
    }
}

