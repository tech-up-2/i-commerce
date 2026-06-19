package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import java.time.LocalDateTime;

public record DeliveryResponse(
        Long orderId,
        Long deliveryId,
        Long storeId,
        DeliveryStatus status,
        LocalDateTime createdAt
) {

    public static DeliveryResponse of(Long orderId, Delivery delivery) {
        return new DeliveryResponse (
                orderId,
                delivery.getId(),
                delivery.getStoreId(),
                delivery.getDeliveryStatus(),
                delivery.getCreatedAt()
        );
    }
}
