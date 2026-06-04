package com.example.i_commerce.domain.chat.service;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatReport;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportStatus;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatReportRepository;
import com.example.i_commerce.domain.chat.service.dto.ChatReportRequest;
import com.example.i_commerce.domain.chat.util.TempChatUtil;
import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatReportService {

    private final ChatReportRepository chatReportRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AdminRepository adminRepository;
    private final MemberService memberService;
    public ApiResponse<Void> createChatReport(ChatReportRequest chatReportRequest) {
        MemberChatInfo member = memberService.getMemberChatInfo(TempChatUtil.getCurrentUserId());
        ChatMessage message = chatMessageRepository.findById(chatReportRequest.messageId())
            .orElseThrow(() -> new AppException(
                ChatErrorCode.MESSAGE_NOT_FOUND));

        validateReport(member.id(), message);

        ChatReport chatReport = ChatReport.builder()
            .reporterId(member.id())
            .reportedId(message.getMemberId())
            .chatMessage(message)
            .chatRoom(message.getChatRoom())
            .originalMessage(message.getContent())
            .reason(chatReportRequest.reason())
            .status(ChatReportStatus.PENDING)
            .build();
        chatReportRepository.save(chatReport);
        return ApiResponse.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> controlReport(Long reportId, ChatReportStatus status) {

        ChatReport chatReport = chatReportRepository.findById(reportId)
            .orElseThrow(() -> new AppException(
                ChatErrorCode.REPORT_NOT_FOUND));
        chatReport.updateStatus(status);
        if (chatReport.getStatus() == ChatReportStatus.RESOLVED) {
            ChatMessage message = chatReport.getChatMessage();
            message.blind();
        }
        chatReportRepository.save(chatReport);
        return ApiResponse.success();
    }

    private void validateReport(Long reporterId, ChatMessage chatMessage) {
//        자신 메시지를 신고하는 경우
        if (chatMessage.getMemberId().equals(reporterId)) {
            throw new AppException(ChatErrorCode.CANNOT_REPORT_SELF);
        }
        if (chatReportRepository.existsByReporterIdAndChatMessageId(reporterId,
            chatMessage.getId())) {
            throw new AppException(ChatErrorCode.DUPLICATE_REPORT);
        }
    }
}
