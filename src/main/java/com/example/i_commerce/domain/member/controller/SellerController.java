package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.seller.SellerService;
import com.example.i_commerce.domain.member.service.seller.dto.SellerInfoResponse;
import com.example.i_commerce.domain.member.service.seller.dto.SellerRequest;
import com.example.i_commerce.domain.member.service.seller.dto.SellerResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 정보, 배송지, 로그인 이력 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller")
public class SellerController {

    private final SellerService sellerService;

    @Operation(summary = "판매자 신청", description = "판매자를 신청한다.")
    @PostMapping("/apply")
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    public ApiResponse<SellerResponse> applyForSeller(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid SellerRequest dto
    ) {
        return ApiResponse.success(sellerService.applyForSeller(principal.getId(), dto));
    }

    @Operation(summary = "판매자 정보 조회", description = "판매자의 정보를 조회한다.")
    @GetMapping("/me")
    @PreAuthorize("@authChecker.canViewSellerInfo()")
    public ApiResponse<SellerInfoResponse> getSellerInfo(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(sellerService.getSellerInfo(principal.getId()));
    }

    @Operation(summary = "판매자 정보 수정", description = "판매자의 정보를 수정한다.")
    @PatchMapping("/me")
    @PreAuthorize("@authChecker.canUpdateSellerInfo()")
    public ApiResponse<SellerResponse> updateSeller(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid SellerRequest dto
    ) {
        return ApiResponse.success(sellerService.updateSeller(principal.getId(), dto));
    }
}