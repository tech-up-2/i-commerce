package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.member.service.store.dto.StoreAddressRequest;
import com.example.i_commerce.domain.member.service.store.dto.StoreAddressResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreInfoResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreRequest;
import com.example.i_commerce.domain.member.service.store.dto.StoreResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreUpdateRequest;
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
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    //상점 개설
    @Operation(summary = "상점 개설", description = "상점을 개설한다.")
    @PreAuthorize("@authChecker.canManageStore()")
    @PostMapping
    public ApiResponse<StoreResponse> createStore(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid StoreRequest dto) {
        return ApiResponse.success(storeService.createStore(principal.getId(), dto));
    }

    //내 상점 목록 조회
    @Operation(summary = "내 상점 목록 조회", description = "내 상점 목록들을 조회한다.")
    @PreAuthorize("@authChecker.canViewStore()")
    @GetMapping("/me")
    public ApiResponse<List<StoreResponse>> getMyStores(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(storeService.getMyStores(principal.getId()));
    }

    //상점 상세 조회
    @Operation(summary = "내 상점의 정보 조회", description = "내 상점의 정보를 조회한다.")
    @PreAuthorize("@authChecker.canViewStore()")
    @GetMapping("/{storeId}")
    public ApiResponse<StoreInfoResponse> getStoreInfo(
        @PathVariable Long storeId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(storeService.getMyStoreInfo(storeId, principal.getId()));
    }

    //상점 정보 수정
    @Operation(summary = "내 상점의 정보 수정", description = "내 상점의 정보를 수정한다.")
    @PreAuthorize("@authChecker.canManageStore()")
    @PatchMapping("/{storeId}")
    public ApiResponse<StoreResponse> updateStore(
        @PathVariable Long storeId,
        @RequestBody @Valid StoreUpdateRequest dto,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(storeService.updateStoreInfo(storeId, dto, principal.getId()));
    }

    //상점 삭제
    @Operation(summary = "내 상점 삭제", description = "내 상점을 삭제한다.")
    @PreAuthorize("@authChecker.canManageStore()")
    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(
        @PathVariable Long storeId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        storeService.deleteStore(storeId, principal.getId());
        return ApiResponse.success();
    }

    //상점 주소 목록 조회
    @Operation(summary = "내 상점의 주소 목록 조회", description = "내 상점의 주소 목록을 조회한다.")
    @PreAuthorize("@authChecker.canViewStore()")
    @GetMapping("/{storeId}/addresses")
    public ApiResponse<List<StoreAddressResponse>> getStoreAddress(
        @PathVariable Long storeId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(storeService.getMyStoreAddresses(storeId, principal.getId()));
    }

    //상점 주소 등록
    @Operation(summary = "내 상점의 주소 등록", description = "내 상점의 주소를 등록한다.")
    @PreAuthorize("@authChecker.canManageStore()")
    @PostMapping("/{storeId}/addresses")
    public ApiResponse<StoreAddressResponse> createStoreAddress(
        @PathVariable Long storeId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid StoreAddressRequest dto
    ) {
        return ApiResponse.success(
            storeService.createStoreAddress(storeId, principal.getId(), dto));
    }

    //상점 주소 수정
    @Operation(summary = "내 상점의 주소 수정", description = "내 상점의 주소를 수정한다.")
    @PreAuthorize("@authChecker.canManageStore()")
    @PatchMapping("/{storeId}/addresses/{addressId}")
    public ApiResponse<StoreAddressResponse> updateStoreAddress(
        @PathVariable Long storeId,
        @PathVariable Long addressId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid StoreAddressRequest dto
    ) {
        return ApiResponse.success(
            storeService.updateStoreAddress(addressId, storeId, principal.getId(), dto));
    }

    //상점 주소 삭제
    @Operation(summary = "내 상점의 주소 삭제", description = "내 상점의 주소를 삭제한다.")
    @PreAuthorize("@authChecker.canManageStore()")
    @DeleteMapping("/{storeId}/addresses/{addressId}")
    public ApiResponse<Void> deleteStoreAddress(
        @PathVariable Long storeId,
        @PathVariable Long addressId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        storeService.deleteStoreAddress(addressId, storeId, principal.getId());
        return ApiResponse.success();
    }

    //기본주소 설정
    @Operation(summary = "내 상점의 기본주소 변경", description = "내 상점의 기본주소를 변경한다.")
    @PreAuthorize("@authChecker.canManageStore()")
    @PatchMapping("/{storeId}/addresses/{addressId}/default")
    public ApiResponse<Void> changeDefault(
        @PathVariable Long storeId,
        @PathVariable Long addressId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        storeService.changeDefault(addressId, storeId, principal.getId());
        return ApiResponse.success();
    }
}
