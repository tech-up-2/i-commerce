package com.example.i_commerce.domain.member.service.loginHistory.dto;

import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import java.time.LocalDateTime;

public record MemberLoginHistoryEvent(
    Long memberId,
    LoginResult loginResult,
    String ipAddress,
    LocalDateTime loginAt,
    LoginFailReason failReason
) {

}