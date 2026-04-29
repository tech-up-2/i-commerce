package com.example.i_commerce.domain.member.entity.enums;

public enum StoreStatus {
    OPEN,
    CLOSE, //외부에서 상점으로 접속불가, 상품 노출안됨
    BLOCKED,
    WITHDRAW //상점이 삭제된 상태
}
