package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.auth.AuthService;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.auth.dto.LoginResponse;
import com.example.i_commerce.domain.member.service.auth.dto.SignUpResponse;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberSignUpRequest;
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
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "일반 회원 계정을 생성한다.")
    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@RequestBody @Valid MemberSignUpRequest dto) {
        SignUpResponse response = authService.signUp(dto);
        return ApiResponse.success(response);
    }

    @Operation(summary = "로그인", description = "회원임을 인증하고 토큰을 발급받는다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest dto) {
        LoginResponse response = authService.login(dto);
        return ApiResponse.success(response);
    }

    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "로그아웃", description = "로그아웃한다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {//나중에 redis를 붙이면 토큰을 blacklist로 전달해야함.
        return ApiResponse.success();
    }

    //복호화 테스트
//    @GetMapping("/getget/{id}")
//    public ApiResponse<MemberOrderInfo> testGetMemberOrderInfo(@PathVariable Long id) {
//        MemberOrderInfo memberOrderInfo = memberService.getMemberOrderInfo(id);
//        return ApiResponse.success(memberOrderInfo);
//    }
}
