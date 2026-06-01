package com.example.i_commerce.domain.order.service.dto;

public record DeliveryShipRequest(
        Long deliveryId,
        String trackingNumber
) {
}
