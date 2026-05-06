package com.example.i_commerce.domain.member.entity.enums;

import com.example.i_commerce.domain.member.tools.AccountRole;

public enum MemberType implements AccountRole {
    CUSTOMER, //일반멤버
    SELLER; //판매자

    @Override
    public String getAuthority() {
        return this.name();
    }
}
