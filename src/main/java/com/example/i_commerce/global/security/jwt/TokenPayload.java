package com.example.i_commerce.global.security.jwt;

import com.example.i_commerce.domain.member.tools.AccountRole;
import com.example.i_commerce.domain.member.tools.AccountStatus;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;

public record TokenPayload(
    PrincipalType principalType,

    Long accountId,
    String email,

    AccountRole role,

    AccountStatus accountStatus,   // memberStatus 또는 adminStatus
    AccountStatus sellerStatus     // 판매자일 때만 사용, 아니면 null
) {

}
