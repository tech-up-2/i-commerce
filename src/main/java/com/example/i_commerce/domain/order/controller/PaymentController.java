package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.facade.PaymentFacade;
import com.example.i_commerce.domain.order.service.PaymentService;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "결제 완료", description = "토스 페이먼츠에서 카드 인증 후 진짜 결제를 위해 호출되는 API")
    @PostMapping("/confirm")
    public ApiResponse<Void> paymentConfirm(
            @RequestBody PaymentConfirmRequest dto
    ) {
        paymentFacade.confirmPayment(dto);
        return ApiResponse.success();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "결제 취소", description = "결제를 취소한다.")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancelPayment(@RequestBody PaymentCancelRequest dto) {
        paymentFacade.cancelPayment(dto);
        return ApiResponse.success();
    }

}
