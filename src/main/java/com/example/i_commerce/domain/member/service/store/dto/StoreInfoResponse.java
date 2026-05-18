package com.example.i_commerce.domain.member.service.store.dto;

import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import java.time.LocalDateTime;

public record StoreInfoResponse(
    Long id,
    Long sellerId,
    String storeName,
    String phoneNumber,
    StoreStatus storeStatus,
    LocalDateTime createdAt
) {

    public static StoreInfoResponse from(Store store) {
        return new StoreInfoResponse(
            store.getId(),
            store.getSellerId(),
            store.getStoreName(),
            store.getPhoneNumber(),
            store.getStoreStatus(),
            store.getCreatedAt()
        );
    }
}
