package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.entity.enums.ChatReportStatus;
import com.example.i_commerce.domain.chat.service.ChatReportService;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat Admin API", description = "관리자 채팅 신고 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/chat/admin")
@RequiredArgsConstructor
public class AdminChatController {
    private final ChatReportService chatReportService;

    @Operation(summary = "관리자 신고 처리", description = "관리자가 신고 상태를 변경 및 처리합니다.")
    @PatchMapping("/report/{reportId}")
    public ApiResponse<Void> controlReport(@PathVariable Long reportId, @RequestParam ChatReportStatus status) {
        return chatReportService.controlReport(reportId, status);
    }
}
