package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.DeliveryAddressService;
import com.example.i_commerce.domain.member.service.dto.DeliveryAddressRequest;
import com.example.i_commerce.domain.member.service.dto.DeliveryAddressResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/delivery-addresses")
@PreAuthorize("@authChecker.canViewMemberInfo()")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    //배송지 목록 조회
    @GetMapping
    public ApiResponse<List<DeliveryAddressResponse>> getMyAddress(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(deliveryAddressService.getMyAddresses(principal.getId()));
    }

    //배송지 등록
    @PostMapping
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    public ApiResponse<DeliveryAddressResponse> createNewAddress(
        @RequestBody @Valid DeliveryAddressRequest dto,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        DeliveryAddressResponse response =
            deliveryAddressService.createNewAddress(dto, principal.getId());
        return ApiResponse.success(response);
    }

    //배송지 수정
    @PatchMapping("/{addressId}")
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    public ApiResponse<DeliveryAddressResponse> updateAddress(
        @PathVariable Long addressId,
        @RequestBody @Valid DeliveryAddressRequest dto,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        DeliveryAddressResponse response =
            deliveryAddressService.updateAddress(addressId, dto, principal.getId());

        return ApiResponse.success(response);
    }

    //기본 배송지 변경
    @PatchMapping("/{addressId}/default")
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    public ApiResponse<Void> changeDefaultAddress(
        @PathVariable Long addressId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        deliveryAddressService.changeDefaultAddress(
            addressId,
            principal.getId()
        );

        return ApiResponse.success();
    }

    //배송지 삭제
    @DeleteMapping("/{addressId}")
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    public ApiResponse<Void> deleteAddress(
        @PathVariable Long addressId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        deliveryAddressService.deleteAddress(addressId, principal.getId());
        return ApiResponse.success();
    }
}
