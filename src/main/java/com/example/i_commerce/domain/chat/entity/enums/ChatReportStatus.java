package com.example.i_commerce.domain.chat.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChatReportStatus {
    PENDING("처리 대기"),
    IN_PROGRESS("처리 중"),
    RESOLVED("처리 완료"),
    REJECTED("반려");

    private final String description;
}