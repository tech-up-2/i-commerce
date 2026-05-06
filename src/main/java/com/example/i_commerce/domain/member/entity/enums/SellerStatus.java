package com.example.i_commerce.domain.member.entity.enums;

import com.example.i_commerce.domain.member.tools.AccountStatus;

public enum SellerStatus implements AccountStatus {
    PENDING, //대기
    APPROVED, //승인됨
    BLOCKED, //정지
    WITHDRAW; //탈퇴

    @Override
    public String getAuthority() {
        return this.name();
    }
}
