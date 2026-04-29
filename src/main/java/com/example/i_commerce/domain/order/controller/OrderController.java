package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<Void> createOrder(CreateOrderRequest dto) {
        return orderService.createOrder(dto);
    }
}
