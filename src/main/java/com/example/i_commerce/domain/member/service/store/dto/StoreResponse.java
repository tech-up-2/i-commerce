package com.example.i_commerce.domain.member.service.store.dto;

import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;

public record StoreResponse(
    Long storeId,
    String storeName,
    StoreStatus storeStatus
) {

    public static StoreResponse from(Store store) {
        return new StoreResponse(
            store.getId(),
            store.getStoreName(),
            store.getStoreStatus()
        );
    }
}
