package com.example.i_commerce.domain.chat.service.dto;

import com.example.i_commerce.domain.chat.entity.enums.ChatReportReason;

public record ChatReportRequest(
    Long messageId,
    ChatReportReason reason
)
{
}
