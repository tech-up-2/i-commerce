package com.example.i_commerce.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.admin.AdminService;
import com.example.i_commerce.domain.member.service.auth.AuthService;
import com.example.i_commerce.domain.member.service.auth.dto.TokenReissueRequest;
import com.example.i_commerce.domain.member.service.auth.dto.TokenReissueResponse;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.domain.testtools.AdminFixture;
import com.example.i_commerce.domain.testtools.MemberFixture;
import com.example.i_commerce.domain.testtools.SellerFixture;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.RefreshTokenValidator;
import com.example.i_commerce.global.security.jwt.dto.RefreshTokenPayload;
import com.example.i_commerce.global.security.jwt.dto.TokenPayload;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceReissueTest {

    @Mock
    private JwtTokenUtil jwtProvider;

    @Mock
    private RefreshTokenValidator refreshTokenValidator;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private AuthService authService;

    @Mock
    private EmailHashEncoder emailHashEncoder;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DataEncryptor dataEncryptor;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("회원 Refresh Token으로 Access Token 재발급 성공")
    void reissue_success_member() {
        // given
        String refreshToken = "member-refresh-token";
        String newAccessToken = "new-access-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            "token-id"
        );

        Member member = MemberFixture.createMember(
            MemberStatus.ACTIVE,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        ReflectionTestUtils.setField(member, "id", 1L);

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.MEMBER))
            .willReturn(refreshPayload);

        given(memberRepository.findById(1L))
            .willReturn(Optional.of(member));

        given(jwtProvider.createToken(any(TokenPayload.class)))
            .willReturn(newAccessToken);

        // when
        TokenReissueResponse response = authService.reissue(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(newAccessToken);
    }

    @Test
    @DisplayName("Refresh Token의 회원 ID에 해당하는 회원이 없으면 예외가 발생한다")
    void reissue_fail_memberNotFound() {
        // given
        String refreshToken = "member-refresh-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            999L,
            "token-id"
        );

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.MEMBER))
            .willReturn(refreshPayload);

        given(memberRepository.findById(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining(MemberErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("탈퇴 회원은 Access Token을 재발급받을 수 없다")
    void reissue_fail_withdrawnMember() {
        // given
        String refreshToken = "member-refresh-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            "token-id"
        );

        Member member = Member.builder()
            .emailHash("emailHash")
            .emailEncrypted(dataEncryptor.encrypt("emailEncrypted"))
            .password("password")
            .name(dataEncryptor.encrypt("name"))
            .birthday(dataEncryptor.encrypt("2000-01-01"))
            .phoneNumber(dataEncryptor.encrypt("01012345678"))
            .sex(Gender.MALE)
            .role(MemberType.CUSTOMER)
            .status(MemberStatus.WITHDRAWN)
            .build();

        ReflectionTestUtils.setField(member, "id", 1L);

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.MEMBER))
            .willReturn(refreshPayload);

        given(memberRepository.findById(1L))
            .willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> authService.reissue(request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 회원은 SellerStatus를 포함해서 Access Token을 재발급한다")
    void reissue_success_sellerMember() {
        // given
        String refreshToken = "seller-refresh-token";
        String newAccessToken = "new-access-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            "token-id"
        );

        Member member = MemberFixture.createSellerMember(
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        ReflectionTestUtils.setField(member, "id", 1L);

        Seller seller = SellerFixture.createSeller(
            member,
            SellerStatus.APPROVED,
            true,
            dataEncryptor
        );

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.MEMBER))
            .willReturn(refreshPayload);

        given(memberRepository.findById(1L))
            .willReturn(Optional.of(member));

        given(sellerRepository.findById(1L))
            .willReturn(Optional.of(seller));

        given(jwtProvider.createToken(any(TokenPayload.class)))
            .willReturn(newAccessToken);

        ArgumentCaptor<TokenPayload> payloadCaptor =
            ArgumentCaptor.forClass(TokenPayload.class);

        // when
        TokenReissueResponse response = authService.reissue(request);

        // then
        assertThat(response.accessToken()).isEqualTo(newAccessToken);

        then(jwtProvider).should().createToken(payloadCaptor.capture());

        TokenPayload capturedPayload = payloadCaptor.getValue();

        assertThat(capturedPayload.principalType()).isEqualTo(PrincipalType.MEMBER);
        assertThat(capturedPayload.accountId()).isEqualTo(1L);
        assertThat(capturedPayload.role()).isEqualTo(MemberType.SELLER);
        assertThat(capturedPayload.accountStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(capturedPayload.sellerStatus()).isEqualTo(SellerStatus.APPROVED);
    }

    @Test
    @DisplayName("관리자 Refresh Token으로 Access Token 재발급 성공")
    void reissue_success_admin() {
        // given
        String refreshToken = "admin-refresh-token";
        String newAccessToken = "admin-new-access-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.ADMIN,
            1L,
            "admin-token-id"
        );

        Admin admin = AdminFixture.createAdmin(
            AdminRole.ADMIN,
            AdminStatus.ACTIVE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        ReflectionTestUtils.setField(admin, "id", 1L);

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.ADMIN))
            .willReturn(refreshPayload);

        given(adminRepository.findById(1L))
            .willReturn(Optional.of(admin));

        given(jwtProvider.createToken(any(TokenPayload.class)))
            .willReturn(newAccessToken);

        // when
        TokenReissueResponse response = adminService.reissue(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(newAccessToken);
    }

    @Test
    @DisplayName("관리자는 최신 role/status 기준으로 Access Token을 재발급한다")
    void reissue_success_adminPayload() {
        // given
        String refreshToken = "admin-refresh-token";
        String newAccessToken = "admin-new-access-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.ADMIN,
            1L,
            "admin-token-id"
        );

        Admin admin = AdminFixture.createAdmin(
            AdminRole.OPERATOR,
            AdminStatus.ACTIVE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        ReflectionTestUtils.setField(admin, "id", 1L);

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.ADMIN))
            .willReturn(refreshPayload);

        given(adminRepository.findById(1L))
            .willReturn(Optional.of(admin));

        given(jwtProvider.createToken(any(TokenPayload.class)))
            .willReturn(newAccessToken);

        ArgumentCaptor<TokenPayload> payloadCaptor =
            ArgumentCaptor.forClass(TokenPayload.class);

        // when
        TokenReissueResponse response = adminService.reissue(request);

        // then
        assertThat(response.accessToken()).isEqualTo(newAccessToken);

        then(jwtProvider).should().createToken(payloadCaptor.capture());

        TokenPayload capturedPayload = payloadCaptor.getValue();

        assertThat(capturedPayload.principalType()).isEqualTo(PrincipalType.ADMIN);
        assertThat(capturedPayload.accountId()).isEqualTo(1L);
        assertThat(capturedPayload.role()).isEqualTo(AdminRole.OPERATOR);
        assertThat(capturedPayload.accountStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(capturedPayload.sellerStatus()).isNull();
    }

    @Test
    @DisplayName("Refresh Token의 관리자 ID에 해당하는 관리자가 없으면 예외가 발생한다")
    void reissue_fail_adminNotFound() {
        // given
        String refreshToken = "admin-refresh-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.ADMIN,
            999L,
            "admin-token-id"
        );

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.ADMIN))
            .willReturn(refreshPayload);

        given(adminRepository.findById(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.reissue(request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("정지 관리자는 Access Token을 재발급받을 수 없다")
    void reissue_fail_suspendedAdmin() {
        // given
        String refreshToken = "admin-refresh-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.ADMIN,
            1L,
            "admin-token-id"
        );

        Admin admin = AdminFixture.createAdmin(
            AdminRole.ADMIN,
            AdminStatus.LOCKED,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        ReflectionTestUtils.setField(admin, "id", 1L);

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.ADMIN))
            .willReturn(refreshPayload);

        given(adminRepository.findById(1L))
            .willReturn(Optional.of(admin));

        // when & then
        assertThatThrownBy(() -> adminService.reissue(request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("탈퇴 관리자는 Access Token을 재발급받을 수 없다")
    void reissue_fail_withdrawnAdmin() {
        // given
        String refreshToken = "admin-refresh-token";

        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        RefreshTokenPayload refreshPayload = new RefreshTokenPayload(
            PrincipalType.ADMIN,
            1L,
            "admin-token-id"
        );

        Admin admin = AdminFixture.createAdmin(
            AdminRole.ADMIN,
            AdminStatus.WITHDRAWN,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        ReflectionTestUtils.setField(admin, "id", 1L);

        given(refreshTokenValidator.validate(refreshToken, PrincipalType.ADMIN))
            .willReturn(refreshPayload);

        given(adminRepository.findById(1L))
            .willReturn(Optional.of(admin));

        // when & then
        assertThatThrownBy(() -> adminService.reissue(request))
            .isInstanceOf(AppException.class);
    }
}