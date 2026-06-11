package com.example.i_commerce.domain.order.event.dto;

import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;

public record DeliveryStatusChangedEvent(
        Long orderId,
        Long deliveryId,
        DeliveryStatus currentStatus
) {
}
