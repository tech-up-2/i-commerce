package com.example.i_commerce.global.security.jwt;

import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.tools.AccountRole;
import com.example.i_commerce.domain.member.tools.AccountStatus;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;

public record TokenPayload(
    PrincipalType principalType,

    Long accountId,

    AccountRole role,

    AccountStatus accountStatus,   // memberStatus 또는 adminStatus
    SellerStatus sellerStatus     // 판매자일 때만 사용, 아니면 null
) {

}
