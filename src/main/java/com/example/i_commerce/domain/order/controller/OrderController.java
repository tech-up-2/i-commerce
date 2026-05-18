package com.example.i_commerce.domain.order.controller;

import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.CreateOrderResponse;
import com.example.i_commerce.domain.order.service.dto.OrderDetailResponse;
import com.example.i_commerce.domain.order.service.dto.OrderSummaryResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Order API", description = "주문 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "주문 생성", description = "주문을 생성한다.")
    @PostMapping
    public ApiResponse<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserPrincipal member,
            @RequestBody CreateOrderRequest dto) {
        return orderService.createOrder(member.getId(), dto);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "주문 요약 목록 조회", description = "주문 요약 정보의 목록을 통해 상세 주문 정보를 조회할 수 있다.")
    @GetMapping
    public ApiResponse<List<OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserPrincipal member
    ) {
        return ApiResponse.success(orderService.getOrderList(member.getId()));
    }

    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "주문 상세 조회", description = "결제 취소에 필요한 상세 주문 정보를 조회한다.")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @AuthenticationPrincipal CustomUserPrincipal member,
            @PathVariable Long orderId
    ) {
            return ApiResponse.success(orderService.getOrderDetail(orderId, member.getId()));
    }
}