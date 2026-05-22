package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.admin.AdminService;
import com.example.i_commerce.domain.member.service.admin.dto.AdminCreateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminCreateResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminInfoResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminMemberResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminMemberStatusUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminRoleUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminSellerResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminSellerStatusUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminStatusUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminUpdateResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.common.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin/manage")
public class AdminManagementController {

    private final AdminService adminService;

    //관리자 생성
    @PreAuthorize("@authChecker.master()")
    @Operation(summary = "관리자 생성", description = "새 관리자 계정을 생성한다.")
    @PostMapping
    public ApiResponse<AdminCreateResponse> createAdmin(
        @Valid @RequestBody AdminCreateRequest request
    ) {
        return ApiResponse.success(adminService.createAdmin(request));
    }

    //관리자 목록 조회
    @GetMapping
    @PreAuthorize("@authChecker.canManageAdmin()")
    public ApiResponse<SliceResponse<AdminInfoResponse>> getAdmins(
        @PageableDefault(size = 20, sort = "createdAt")
        Pageable pageable
    ) {
        return ApiResponse.success(adminService.getAdmins(pageable));
    }

    //관리자 권한 변경
    @PatchMapping("/{adminId}/role")
    @PreAuthorize("@authChecker.canManageAdmin()")
    public ApiResponse<AdminUpdateResponse> updateAdminRole(
        @PathVariable Long adminId,
        @Valid @RequestBody AdminRoleUpdateRequest request
    ) {
        return ApiResponse.success(adminService.updateAdminRole(adminId, request));
    }

    //관리자 상태 변경
    @PatchMapping("/{adminId}/status")
    @PreAuthorize("@authChecker.canManageAdmin()")
    public ApiResponse<AdminUpdateResponse> updateAdminStatus(
        @PathVariable Long adminId,
        @Valid @RequestBody AdminStatusUpdateRequest request
    ) {
        return ApiResponse.success(adminService.updateAdminStatus(adminId, request));
    }

    //관리자 로그인 이력 관리

    
    //관리자-사용자 관리

    /**
     * 사용자 상세 조회 MASTER / ADMIN / OPERATOR 가능
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("@authChecker.canManageUserAndSeller()")
    public ApiResponse<AdminMemberResponse> getMember(
        @PathVariable Long userId
    ) {
        return ApiResponse.success(adminService.getMember(userId));
    }

    /**
     * 사용자 상태 변경 MASTER / ADMIN / OPERATOR 가능
     */
    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("@authChecker.canManageUserAndSeller()")
    public ApiResponse<AdminMemberResponse> updateMemberStatus(
        @PathVariable Long userId,
        @Valid @RequestBody AdminMemberStatusUpdateRequest request
    ) {
        return ApiResponse.success(adminService.updateMemberStatus(userId, request));
    }


    //관리자-판매자 관리

    /**
     * 판매자 상세 조회 MASTER / ADMIN / OPERATOR 가능
     */
    @GetMapping("/sellers/{sellerId}")
    @PreAuthorize("@authChecker.canManageUserAndSeller()")
    public ApiResponse<AdminSellerResponse> getSeller(
        @PathVariable Long sellerId
    ) {
        return ApiResponse.success(adminService.getSeller(sellerId));
    }

    /**
     * 판매자 상태 변경 MASTER / ADMIN / OPERATOR 가능
     */
    @PatchMapping("/sellers/{sellerId}/status")
    @PreAuthorize("@authChecker.canManageUserAndSeller()")
    public ApiResponse<AdminSellerResponse> updateSellerStatus(
        @PathVariable Long sellerId,
        @Valid @RequestBody AdminSellerStatusUpdateRequest request
    ) {
        return ApiResponse.success(adminService.updateSellerStatus(sellerId, request));
    }
}
