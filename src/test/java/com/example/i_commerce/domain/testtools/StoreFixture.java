package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import java.util.UUID;

public class StoreFixture {

    public static Store createStore(
        Long sellerId,
        StoreStatus storeStatus
    ) {
        String storeName = "testStore-" + UUID.randomUUID();

        return Store.builder()
            .sellerId(sellerId)
            .storeName(storeName)
            .phoneNumber("021234567")
            .storeStatus(storeStatus)
            .build();
    }
}
