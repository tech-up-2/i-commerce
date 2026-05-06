package com.example.i_commerce.domain.member.controller;


import com.example.i_commerce.domain.member.service.AdminService;
import com.example.i_commerce.domain.member.service.dto.AdminLoginResponse;
import com.example.i_commerce.domain.member.service.dto.LoginRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;



    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@RequestBody @Valid LoginRequest dto) {
        AdminLoginResponse adminLoginResponse = adminService.login(dto);
        return ApiResponse.success(adminLoginResponse);
    }
}
