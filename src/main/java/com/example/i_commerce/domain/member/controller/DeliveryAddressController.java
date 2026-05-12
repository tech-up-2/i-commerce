package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.delivery.DeliveryAddressService;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressRequest;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressResponse;
import com.example.i_commerce.domain.member.service.delivery.dto.UpdateDeliveryAddressRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Member API", description = "회원 정보, 배송지, 로그인 이력 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/delivery-addresses")
@PreAuthorize("@authChecker.canViewMemberInfo()")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    //배송지 목록 조회
    @Operation(summary = "배송지 목록 조회", description = "배송지 목록을 조회한다.")
    @GetMapping
    public ApiResponse<List<DeliveryAddressResponse>> getMyAddress(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(deliveryAddressService.getMyAddresses(principal.getId()));
    }

    //배송지 등록
    @Operation(summary = "새 배송지 등록", description = "새 배송지를 등록한다.")
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
    @Operation(summary = "배송지 수정", description = "배송지를 수정한다.")
    @PatchMapping("/{addressId}")
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    public ApiResponse<DeliveryAddressResponse> updateAddress(
        @PathVariable Long addressId,
        @RequestBody UpdateDeliveryAddressRequest dto,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        DeliveryAddressResponse response =
            deliveryAddressService.updateAddress(addressId, dto, principal.getId());

        return ApiResponse.success(response);
    }

    //기본 배송지 변경
    @Operation(summary = "기본 배송지 변경", description = "기본 배송지 변경를 변경한다.")
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
    @Operation(summary = "배송지 삭제", description = "등록되어 있는 배송지를 삭제한다.")
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
