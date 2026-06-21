package com.example.i_commerce.domain.chat.unit.service.oldtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportReason;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatReportRepository;
import com.example.i_commerce.domain.chat.service.ChatReportService;
import com.example.i_commerce.domain.chat.service.dto.ChatReportRequest;
import com.example.i_commerce.domain.chat.unit.service.fixture.ChatMemberFixture;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import java.util.Optional;
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
public class ChatReportServiceTest {

    @InjectMocks
    private ChatReportService chatReportService;

    @Mock
    private ChatReportRepository chatReportRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private MemberService memberService;

    private Member member;
    private ChatRoom chatRoom;
    private ChatMessage chatMessage;
    private MemberChatInfo memberChatInfo;


    @BeforeEach
    public void init() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(PrincipalType.MEMBER, 1L,
            List.of());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(customUserPrincipal, null, List.of()));

        member = ChatMemberFixture.createMember(1L, "user1@naver.com");
        memberChatInfo = new MemberChatInfo
            (member.getId(), "user1", member.getRole(), member.getStatus());
        chatRoom = ChatRoom.builder()
            .id(1L)
            .name("테스트 채팅방")
            .isGroupChat(true)
            .build();
        chatMessage = ChatMessage.builder()
            .id(1L)
            .chatRoom(chatRoom)
            .memberId(2L)
            .content("테스트 메시지입니다.")
            .build();
    }

    @Test
    @DisplayName("신고가 정상적으로 생성됩니다.")
    public void createChatReport() {
        ChatReportRequest request = new ChatReportRequest(1L, ChatReportReason.SWEARWORD);
        when(memberService.getMemberChatInfo(1L)).thenReturn(memberChatInfo);
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage));
        when(chatReportRepository.existsByReporterIdAndChatMessageId(1L,
            chatMessage.getId())).thenReturn(false);

        ApiResponse<Void> response = chatReportService.createChatReport(request);

        assertEquals("SUCCESS", response.code());
    }
}
