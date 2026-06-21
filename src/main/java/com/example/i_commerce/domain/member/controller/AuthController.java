package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.auth.AuthService;
import com.example.i_commerce.domain.member.service.auth.dto.AccountFindEmailRequest;
import com.example.i_commerce.domain.member.service.auth.dto.AccountFindEmailResponse;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.auth.dto.LoginResponse;
import com.example.i_commerce.domain.member.service.auth.dto.MemberSignUpRequest;
import com.example.i_commerce.domain.member.service.auth.dto.PasswordFindRequest;
import com.example.i_commerce.domain.member.service.auth.dto.PasswordResetRequest;
import com.example.i_commerce.domain.member.service.auth.dto.SignUpResponse;
import com.example.i_commerce.domain.member.service.auth.dto.TokenLogoutRequest;
import com.example.i_commerce.domain.member.service.auth.dto.TokenReissueRequest;
import com.example.i_commerce.domain.member.service.auth.dto.TokenReissueResponse;
import com.example.i_commerce.domain.member.service.auth.dto.UserInfoResponse;
import com.example.i_commerce.domain.member.service.auth.dto.UserUpdateRequest;
import com.example.i_commerce.domain.member.service.auth.dto.WithDrawRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 정보, 배송지, 로그인 이력 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

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

    @PostMapping("/reissue")
    public ApiResponse<TokenReissueResponse> reissue(
        @Valid @RequestBody TokenReissueRequest request
    ) {
        return ApiResponse.success(authService.reissue(request));
    }

    //로그아웃
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "로그아웃", description = "로그아웃한다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @Valid @RequestBody TokenLogoutRequest request
    ) {//나중에 redis를 붙여야 함.
        authService.logout(authorization, request);
        return ApiResponse.success();
    }

    //계정 찾기
    @Operation(summary = "이메일 찾기", description = "이름과 전화번호로 가입된 이메일을 찾습니다.")
    @PostMapping("/find/account")
    public ApiResponse<AccountFindEmailResponse> findEmail(
        @Valid @RequestBody AccountFindEmailRequest request
    ) {
        AccountFindEmailResponse response = authService.findEmail(request);
        return ApiResponse.success(response);
    }

    //비밀번호 찾기
    @Operation(summary = "비밀번호 찾기", description = "이메일, 이름, 전화번호 확인 후 새 비밀번호로 변경합니다.")
    @PatchMapping("/find/password")
    public ApiResponse<Void> findPassword(
        @Valid @RequestBody PasswordFindRequest request
    ) {
        authService.findPassword(request);
        return ApiResponse.success(null);
    }

    //비밀번호 재설정
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    @Operation(summary = "비밀번호 재설정", description = "마이페이지에서 새 비밀번호로 변경합니다.")
    @PatchMapping("/password/reset")
    public ApiResponse<Void> resetPassword(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(principal.getId(), request);
        return ApiResponse.success(null);
    }

    //회원 탈퇴
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    @Operation(summary = "회원 탈퇴", description = "회원에서 탈퇴한다.")
    @DeleteMapping("/users/me")
    public ApiResponse<Void> withdraw(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid WithDrawRequest dto
    ) {
        authService.withdraw(principal.getId(), dto);
        return ApiResponse.success(null);
    }

    //회원정보조회
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 회원 정보를 조회합니다.")
    @GetMapping("/users/me")
    @PreAuthorize("@authChecker.canViewMemberInfo()")
    public ApiResponse<UserInfoResponse> getMyInfo(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserInfoResponse response = authService.getMyInfo(principal.getId());
        return ApiResponse.success(response);
    }

    //회원정보수정
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 회원 정보를 수정합니다.")
    @PatchMapping("/users/me")
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    public ApiResponse<UserInfoResponse> updateMyInfo(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        UserInfoResponse response = authService.updateMyInfo(principal.getId(), request);
        return ApiResponse.success(response);
    }

    //Access 토큰 테스트 API
    @PreAuthorize("@authChecker.canUpdateMemberInfo()")
    @GetMapping("/tokentest")
    public ApiResponse<String> accessTokenTest() {
        return ApiResponse.success("테스트 성공");
    }
}
