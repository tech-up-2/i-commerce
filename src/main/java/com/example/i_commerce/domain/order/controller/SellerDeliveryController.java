package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.service.SellerDeliveryService;
import com.example.i_commerce.domain.order.service.dto.DeliveryShipRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deliveries")
public class SellerDeliveryController {

    private final SellerDeliveryService sellerDeliveryService;

    @PostMapping("/ship")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> shipProduct(@RequestBody DeliveryShipRequest request) {
        sellerDeliveryService.shipDelivery(request);
        return ApiResponse.success();
    }



}
