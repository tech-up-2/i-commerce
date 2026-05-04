package com.example.i_commerce.domain.member.entity.enums;

import com.example.i_commerce.domain.member.tools.AccountStatus;

public enum AdminStatus implements AccountStatus {
    ACTIVE, //정상
    SUSPENDED, //정지
    WITHDRAWN; //탈퇴

    @Override
    public String getAuthority() {
        return this.name();
    }
}
