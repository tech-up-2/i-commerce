package com.example.i_commerce.domain.member.entity.enums;

import com.example.i_commerce.domain.member.tools.AccountRole;

public enum AdminRole implements AccountRole {
    MASTER, //가장 높음
    ADMIN,
    OPERATOR; //가장 낮음

    @Override
    public String getAuthority() { return this.name(); }
}
