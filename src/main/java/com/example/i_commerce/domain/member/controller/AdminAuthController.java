package com.example.i_commerce.domain.member.controller;


import com.example.i_commerce.domain.member.service.admin.AdminService;
import com.example.i_commerce.domain.member.service.admin.dto.AdminLoginResponse;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 정보, 배송지, 로그인 이력 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;

    @Operation(summary = "관리자 로그인", description = "관리자임을 인증하고 토큰을 발급받는다.")
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@RequestBody @Valid LoginRequest dto) {
        AdminLoginResponse adminLoginResponse = adminService.login(dto);
        return ApiResponse.success(adminLoginResponse);
    }

    @PreAuthorize("isAuthenticated()")//로그인 되어 있는지 확인
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "관리자 로그아웃", description = "로그아웃한다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {//나중에 redis를 붙이면 토큰을 blacklist로 전달해야함.
        return ApiResponse.success();
    }

    //관리자 목록 조회
    //관리자 권한 변경
    //관리자 상태 변경
    //관리자 로그인 이력 관리
    //관리자-사용자 관리
    //관리자-판매자 관리
}
