package com.example.i_commerce.domain.chat.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


public record ChatMessageSendResponse(
    String message,
    Long senderId
)
{}