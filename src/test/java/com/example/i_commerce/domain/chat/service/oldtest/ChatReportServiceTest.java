package com.example.i_commerce.domain.chat.service.oldtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportReason;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatReportRepository;
import com.example.i_commerce.domain.chat.service.ChatReportService;
import com.example.i_commerce.domain.chat.service.dto.ChatReportRequest;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.repository.MemberRepository;
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
    private MemberRepository memberRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;

    private Member member;
    private ChatRoom chatRoom;
    private ChatMessage chatMessage;

    @BeforeEach
    public void init() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(PrincipalType.MEMBER, 1L,
            List.of());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(customUserPrincipal, null, List.of()));

        member = Member.builder()
            .id(1L)
            .emailHash("user1@test.com")
            .password("1234")
            .name("테스트 유저1".getBytes())
            .sex(Gender.MALE)
            .birthday("20050505".getBytes())
            .phoneNumber("01011111111".getBytes())
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.CUSTOMER)
            .build();
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
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(chatMessage));
        when(chatReportRepository.existsByReporterIdAndChatMessageId(1L,
            chatMessage.getId())).thenReturn(false);

        ApiResponse<Void> response = chatReportService.createChatReport(request);

        assertEquals("SUCCESS", response.code());
    }
}
