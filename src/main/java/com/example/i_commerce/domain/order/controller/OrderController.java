package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Order API", description = "주문 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "주문을 생성한다.")
    @PostMapping
    public ApiResponse<Void> createOrder(CreateOrderRequest dto) {
        return orderService.createOrder(dto);
    }
}