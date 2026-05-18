package com.example.i_commerce.domain.member.entity.enums;

public enum LoginFailReason {
    INVALID_CREDENTIALS, //email 불일치
    ACCOUNT_NOT_FOUND, //계정 없음
    ACCOUNT_SUSPENDED, //정지된 계정
    ACCOUNT_WITHDRAWN, //탈퇴한 계정
    ACCOUNT_DORMANT, //휴면 계정
    PASSWORD_MISMATCH //비밀번호 불일치
}