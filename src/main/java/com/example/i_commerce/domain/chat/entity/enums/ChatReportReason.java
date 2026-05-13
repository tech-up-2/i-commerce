package com.example.i_commerce.domain.chat.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ChatReportReason {
    SWEARWORD("비속어"),
    SPAM("스팸/광고"),
    FRAUD("사기"),
    OTHER("기타");

    private final String description;
}


