package com.example.i_commerce.domain.chat.service.dto;

import lombok.Builder;

@Builder
public record MyChatListResponse(
    Long roomId,
    String roomName,
    Boolean isGroupChat,
    Long unReadCount
) {

}
