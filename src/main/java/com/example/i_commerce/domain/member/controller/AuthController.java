package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.AuthService;
import com.example.i_commerce.domain.member.service.MemberService;
import com.example.i_commerce.domain.member.service.dto.MemberSignUpRequest;
import com.example.i_commerce.domain.member.service.dto.SignUpResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@RequestBody @Valid MemberSignUpRequest dto) {

        SignUpResponse response = authService.signUp(dto);
        return ApiResponse.success(response);
    }

//    @PostMapping("/login")
//    public ApiResponse<SignUpResponse> signUp(@RequestBody @Valid LoginRequest dto) {
//
//    }

    //복호화 테스트
//    @GetMapping("/getget/{id}")
//    public ApiResponse<MemberOrderInfo> testGetMemberOrderInfo(@PathVariable Long id) {
//        MemberOrderInfo memberOrderInfo = memberService.getMemberOrderInfo(id);
//        return ApiResponse.success(memberOrderInfo);
//    }
}
