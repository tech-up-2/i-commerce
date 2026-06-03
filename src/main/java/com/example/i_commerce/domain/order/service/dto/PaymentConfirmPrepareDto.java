package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.product.application.dto.StockDeductCommand;
import java.util.List;

public record PaymentConfirmPrepareDto(
        Long paymentId,
        String tossOrderId,
        List<StockDeductCommand> commands

) {
    public static PaymentConfirmPrepareDto of(Payment payment, List<StockDeductCommand> commands) {
        return new PaymentConfirmPrepareDto(payment.getId(), payment.getTossOrderId(), commands);
    }
}
