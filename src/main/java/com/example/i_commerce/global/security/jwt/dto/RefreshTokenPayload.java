package com.example.i_commerce.global.security.jwt.dto;

import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;

public record RefreshTokenPayload(
    PrincipalType principalType,

    Long accountId,

    String tokenId
) {

}
