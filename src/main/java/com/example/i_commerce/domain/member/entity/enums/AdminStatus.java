package com.example.i_commerce.domain.member.entity.enums;

import com.example.i_commerce.domain.member.tools.AccountStatus;

public enum AdminStatus implements AccountStatus {
    ACTIVE, // 정상
    LOCKED, // 계정 잠금
    WITHDRAWN; // 탈퇴/삭제 처리

    @Override
    public String getAuthority() {
        return this.name();
    }
}
