package com.example.i_commerce.domain.member.service.member.dto;

import com.example.i_commerce.domain.member.tools.AccountRole;
import com.example.i_commerce.domain.member.tools.AccountStatus;

public record MemberChatInfo(
    Long id,
    String name,
    AccountRole role,
    AccountStatus status
) {

}
