package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.service.SellerDeliveryService;
import com.example.i_commerce.domain.order.service.dto.DeliveryResponse;
import com.example.i_commerce.domain.order.service.dto.DeliveryShipRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deliveries")
public class SellerDeliveryController {

    private final SellerDeliveryService sellerDeliveryService;

    @GetMapping("/{storeId}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Page<DeliveryResponse>> getDeliveries(
            @AuthenticationPrincipal CustomUserPrincipal seller,
            @PathVariable Long storeId,
            @RequestParam(required = false) DeliveryStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return sellerDeliveryService.getDeliveryList(seller.getId(), storeId, status, pageable);
    }

    @PatchMapping("/ship")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> shipProduct(
            @AuthenticationPrincipal CustomUserPrincipal seller,
            @RequestBody DeliveryShipRequest request) {
        return sellerDeliveryService.shipDelivery(seller.getId(), request);
    }
}

// 할거
// 배송 서비스 가짜 서버 만드는거 고려해보기