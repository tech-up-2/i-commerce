package com.example.i_commerce.domain.chat.service.dto;



public record ChatMessageSendRequest (
    String message,
    Long senderId
)
{}
