package com.example.i_commerce.domain.chat.service;

import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatReport;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportReason;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportStatus;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatReportRepository;
import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
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
public class ChatAdminReportTest {

    @InjectMocks
    private ChatReportService chatReportService;

    @Mock
    private ChatReportRepository chatReportRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;

    private Admin admin;
    private ChatRoom chatRoom;
    private ChatMessage chatMessage;
    private ChatReport chatReport;

    @BeforeEach
    public void init() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(PrincipalType.ADMIN, 1L,
            List.of());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(customUserPrincipal, null, List.of()));

        admin = Admin.builder()
            .id(1L)
            .emailHash("admin1@test.com")
            .password("1234")
            .name("어드민 유저1".getBytes())
            .adminStatus(AdminStatus.ACTIVE)
            .adminRole(AdminRole.MASTER)
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
        chatReport = ChatReport.builder()
            .id(1L)
            .chatMessage(chatMessage)
            .reporterId(1L)
            .reportedId(2L)
            .reason(ChatReportReason.SWEARWORD)
            .status(ChatReportStatus.PENDING)
            .build();
    }

    @Test
    @DisplayName("관리자가 유저의 채팅의 신고를 정상적으로 처리할 수 있다.")
    public void controlReportTest() {
        when(chatReportRepository.findById(chatReport.getId())).thenReturn(Optional.of(chatReport));

        ApiResponse<Void> response = chatReportService.controlReport(chatReport.getId(),
            ChatReportStatus.RESOLVED);

        Assertions.assertEquals("SUCCESS", response.code());
        Assertions.assertEquals(ChatReportStatus.RESOLVED, chatReport.getStatus());
        Assertions.assertTrue(chatMessage.isBlind());
    }
}
