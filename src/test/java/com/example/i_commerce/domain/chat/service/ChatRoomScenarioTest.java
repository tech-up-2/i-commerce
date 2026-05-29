package com.example.i_commerce.domain.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.repository.ChatParticipantRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.service.fixture.ChatMemberFixture;
import com.example.i_commerce.domain.chat.service.fixture.ChatRoomFixture;
import com.example.i_commerce.domain.chat.util.ChatRoleChecker;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.mysema.commons.lang.Assert;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ChatRoomScenarioTest {

    @InjectMocks
    ChatRoomService chatRoomService;
    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    ChatParticipantRepository chatParticipantRepository;
    @Mock
    ChatRoomNameGenerator chatRoomNameGenerator;
    @Mock
    ChatRoleChecker chatRoleChecker;

    private Member member;
    private Member member2;
    private Member member3;
    private Member member4;
    private ChatRoom singleChatRoom;
    private ChatRoom groupChatRoom;
    private CustomUserPrincipal customUserPrincipal;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(ChatMemberFixture.createPrincipal(), null,
                List.of())
        );
        member = ChatMemberFixture.createMember(1L, "user1@naver.com");
        member2 = ChatMemberFixture.createMember(2L, "user2@naver.com");
        member3 = ChatMemberFixture.createSeller(3L, "seller1@naver.com");
        member4 = ChatMemberFixture.createSeller(4L, "seller4@naver.com");
        singleChatRoom = ChatRoomFixture.createChatPrivateRoom(1L, "1:1 테스트 채팅방");
        groupChatRoom = ChatRoomFixture.createChatGroupRoom(1L, "상품 그룹 테스트 채팅방", 1L);

    }

    @Test
    @DisplayName("시나리오 1 [성공]: 기존 대화하는 채팅방이 없는 구매자와 판매자가 처음으로 1:1 대화를 시작할 때")
    void getCreatePrivateRoom_Success() {
//      Given
        Long otherMemberId = member3.getId();
        String PrivateRoomName = "user1이 요청한 seller1님과의 채팅";

//      When
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.findById(otherMemberId)).thenReturn(Optional.of(member3));
        when(chatParticipantRepository.findExistingPrivateRoom(1L, otherMemberId)).thenReturn(
            Optional.empty());
        when(chatRoomNameGenerator.getPrivateRoomName(any(), any())).thenReturn(PrivateRoomName);
        when(chatRoomRepository.save(any())).thenReturn(singleChatRoom);
        ApiResponse<Long> response = chatRoomService.getOrCreatePrivateRoom(otherMemberId);
//      Then
        Assertions.assertEquals(response.code(), "SUCCESS");

    }


}
