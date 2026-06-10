package com.example.i_commerce.domain.order.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeliveryShipRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,
        @NotNull(message = "상점 ID는 필수입니다.")
        Long storeId,
        @NotNull(message = "배송 ID는 필수입니다.")
        Long deliveryId,
        @NotBlank(message = "송장 번호는 필수입니다.")
        String trackingNumber
) {
}
