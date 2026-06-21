package com.example.i_commerce.domain.chat.unit.service;

import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatReport;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportReason;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportStatus;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatReportRepository;
import com.example.i_commerce.domain.chat.service.ChatReportService;
import com.example.i_commerce.domain.chat.service.dto.ChatReportRequest;
import com.example.i_commerce.domain.chat.unit.service.fixture.ChatMemberFixture;
import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
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
public class ChatReportScenarioTest {
    @InjectMocks
    ChatReportService chatReportService;
    @Mock
    private ChatReportRepository chatReportRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private MemberService memberService;

    private Member reporter;
    private Member member;
    private Admin admin;
    private MemberChatInfo reporterChatInfo;
    private ChatRoom chatRoom;
    private ChatMessage targetMessage;
    private ChatReport pendingReport;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(ChatMemberFixture.createPrincipal(), null, List.of())
        );

        reporter = ChatMemberFixture .createMember(1L, "reporter@naver.com");
        member = ChatMemberFixture.createMember(2L, "user1@naver.com");
        admin = ChatMemberFixture.createAdmin(1L, "admin1@test.com");

        reporterChatInfo = new MemberChatInfo(reporter.getId(), "신고자", reporter.getRole(), reporter.getStatus());
        chatRoom = ChatRoom.builder().id(1L).name("테스트 방").isGroupChat(true).build();

        targetMessage = ChatMessage.builder()
            .id(100L)
            .chatRoom(chatRoom)
            .memberId(member.getId())
            .content("불건전한 메시지 텍스트")
            .isBlind(false)
            .build();

        pendingReport = ChatReport.builder()
            .id(500L)
            .chatMessage(targetMessage)
            .reporterId(reporter.getId())
            .reportedId(member.getId())
            .reason(ChatReportReason.SWEARWORD)
            .status(ChatReportStatus.PENDING)
            .build();
    }
    @Test
    @DisplayName("시나리오 1 [성공]: 다른 유저가 작성한 불건전한 메시지를 정상적으로 신고 접수할 수 있다.")
    void createChatReport_Success() {

        ChatReportRequest request = new ChatReportRequest(targetMessage.getId(), ChatReportReason.SWEARWORD);

        when(memberService.getMemberChatInfo(reporter.getId())).thenReturn(reporterChatInfo);
        when(chatMessageRepository.findById(targetMessage.getId())).thenReturn(Optional.of(targetMessage));
        when(chatReportRepository.existsByReporterIdAndChatMessageId(reporter.getId(), targetMessage.getId()))
            .thenReturn(false);


        ApiResponse<Void> response = chatReportService.createChatReport(request);


        Assertions.assertEquals("SUCCESS", response.code());

    }
    @Test
    @DisplayName("시나리오 2 [예외]:존재하지 않는 채팅을 신고할 수 없습니다. ")
    void createChatReport_Fail_NotFound(){
        Long messageId = targetMessage.getId();

        ChatReportRequest request = new ChatReportRequest(messageId, ChatReportReason.SWEARWORD);

        when(memberService.getMemberChatInfo(reporter.getId())).thenReturn(reporterChatInfo);
        when(chatMessageRepository.findById(targetMessage.getId())).thenReturn(Optional.empty());

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> chatReportService.createChatReport(request));
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.MESSAGE_NOT_FOUND);
    }

    @Test
    @DisplayName("시나리오 3 [예외]: 내 자신의 채팅을 신고 접수할 수 없습니다.")
    void createChatReport_Fail_Self_Message(){
        Long messageId = targetMessage.getId();
        Long memberId = reporter.getId();
        ChatReportRequest request = new ChatReportRequest(messageId, ChatReportReason.SWEARWORD);

        ChatMessage myMessage = ChatMessage.builder()
            .id(messageId)
            .memberId(memberId)
            .content("내 채팅 테스트 채팅")
            .build();

        when(memberService.getMemberChatInfo(reporter.getId())).thenReturn(reporterChatInfo);
        when(chatMessageRepository.findById(targetMessage.getId())).thenReturn(Optional.of(myMessage));

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> chatReportService.createChatReport(request));
        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.CANNOT_REPORT_SELF);
    }
    @Test
    @DisplayName("시나리오4 [예외]: 이미 신고가 접수된 내용의 메시지를 다시 신고하려는 경우")
    void createChatReport_Fail_Duplicated_Report(){
        ChatReportRequest request = new ChatReportRequest(targetMessage.getId(), ChatReportReason.SWEARWORD);

        when(memberService.getMemberChatInfo(reporter.getId())).thenReturn(reporterChatInfo);
        when(chatMessageRepository.findById(targetMessage.getId())).thenReturn(Optional.of(targetMessage));
//      메시지가 이미 신고 접수되어있는 것을 가정
        when(chatReportRepository.existsByReporterIdAndChatMessageId(reporter.getId(), targetMessage.getId()))
            .thenReturn(true);
        AppException exception = Assertions.assertThrows(AppException.class,
            () -> chatReportService.createChatReport(request));

        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.DUPLICATE_REPORT);
    }
    private void changeSecurityContextToAdmin() {
        // 관리자 테스트용 SecurityContext 전환 메서드
        CustomUserPrincipal adminPrincipal = new CustomUserPrincipal(PrincipalType.ADMIN, admin.getId(), List.of());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(adminPrincipal, null, List.of())
        );
    }

    @Test
    @DisplayName("시나리오 5 [성공]: 관리자가 유저의 채팅 신고를 승인(RESOLVED)하면, 신고처리가 완료되고, 메시지가 블라인드 됩니다.")
    void controlReportTest_Resolve_Success() {
        changeSecurityContextToAdmin(); // 어드민 권한 상태로 스위칭
        when(chatReportRepository.findById(pendingReport.getId())).thenReturn(Optional.of(pendingReport));

        ApiResponse<Void> response = chatReportService.controlReport(pendingReport.getId(), ChatReportStatus.RESOLVED);

        Assertions.assertEquals("SUCCESS", response.code());
        Assertions.assertEquals(pendingReport.getStatus(), ChatReportStatus.RESOLVED);
    }

    @Test
    @DisplayName("시나리오 6 [성공]: 관리자가 유저의 채팅 신고를 반려(REJECTED)하면, 신고 상태만 바뀌고 메시지는 변하지 않습니다.")
    void controlReportTest_Reject_Success() {
        changeSecurityContextToAdmin();
        when(chatReportRepository.findById(pendingReport.getId())).thenReturn(Optional.of(pendingReport));

        ApiResponse<Void> response = chatReportService.controlReport(pendingReport.getId(), ChatReportStatus.REJECTED);

        Assertions.assertEquals("SUCCESS", response.code());
        Assertions.assertEquals(pendingReport.getStatus(), ChatReportStatus.REJECTED);
    }

    @Test
    @DisplayName("시나리오 7 [예외]: 존재하지 않는 신고 내역 ID를 처리하려고 할 경우 예외가 발생합니다.")
    void controlReportTest_Fail_NotFound() {
        changeSecurityContextToAdmin();
        Long reportId = 999L;
        when(chatReportRepository.findById(reportId)).thenReturn(Optional.empty());

        AppException exception = Assertions.assertThrows(AppException.class,
            () -> chatReportService.controlReport(reportId, ChatReportStatus.RESOLVED));

        Assertions.assertEquals(exception.getErrorCode(), ChatErrorCode.REPORT_NOT_FOUND);
    }

}
