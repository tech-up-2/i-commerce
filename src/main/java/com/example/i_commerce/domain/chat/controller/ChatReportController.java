package com.example.i_commerce.domain.chat.controller;

import com.example.i_commerce.domain.chat.entity.ChatReport;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportReason;
import com.example.i_commerce.domain.chat.service.ChatReportService;
import com.example.i_commerce.domain.chat.service.dto.ChatReportRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat REPORT API", description = "채팅 신고 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatReportController {

    public final ChatReportService chatReportService;

    @Operation(summary = "유저 신고 생성", description = "채팅에 대해 신고를 진행할 수 있다.")
    @PostMapping("/report/create")
    public ApiResponse<Void> createChatReport(@RequestBody ChatReportRequest chatReportRequest) {
        chatReportService.createChatReport(chatReportRequest);
        return ApiResponse.success();
    }
}
