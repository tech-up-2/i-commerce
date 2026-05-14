package com.example.i_commerce.domain.order.event.dto;

import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;

public record PaymentStatusChangedEvent(
        Payment payment,
        PaymentStatus previousStatus,
        String reason,
        PaymentStatus currentStatus,
        String pgTid,
        String rawData
) {


}
