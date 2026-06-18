package com.example.i_commerce.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.repository.UserLoginHistoryRepository;
import com.example.i_commerce.domain.member.service.auth.AuthService;
import com.example.i_commerce.domain.member.service.auth.dto.AccountFindEmailRequest;
import com.example.i_commerce.domain.member.service.auth.dto.AccountFindEmailResponse;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.auth.dto.LoginResponse;
import com.example.i_commerce.domain.member.service.auth.dto.MemberSignUpRequest;
import com.example.i_commerce.domain.member.service.auth.dto.PasswordFindRequest;
import com.example.i_commerce.domain.member.service.auth.dto.PasswordResetRequest;
import com.example.i_commerce.domain.member.service.auth.dto.SignUpResponse;
import com.example.i_commerce.domain.member.service.auth.dto.UserUpdateRequest;
import com.example.i_commerce.domain.member.service.auth.dto.WithDrawRequest;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.domain.testtools.IntegrationTestSupport;
import com.example.i_commerce.domain.testtools.MemberFixture;
import com.example.i_commerce.domain.testtools.SellerFixture;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "file:.env")
class AuthServiceTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SellerRepository sellerRepository;

    @Autowired
    UserLoginHistoryRepository userLoginHistoryRepository;

    @Autowired
    EmailHashEncoder emailHashEncoder;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DataEncryptor dataEncryptor;

    /*
    회원가입 메서드 테스트
     */
    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {
        MemberSignUpRequest request = createSignUpRequest(
            "signup@test.com",
            "password123!"
        );

        SignUpResponse response = authService.signUp(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("signup@test.com");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_duplicatedEmail() {
        // given
        MemberSignUpRequest request = createSignUpRequest(
            "duplicate@test.com",
            "password123!"
        );

        authService.signUp(request);

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> authService.signUp(request)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @Disabled
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("회원가입 동시성 테스트")
    void signUp_fail_Email() throws InterruptedException {
        // given
        MemberSignUpRequest request = createSignUpRequest(
            "duplicated@test.com",
            "password123!"
        );

        int threadCount = 10;

        CountDownLatch readySignal = new CountDownLatch(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    readySignal.countDown();
                    startSignal.await();

                    authService.signUp(request);
                    successCount.incrementAndGet();
                } catch (AppException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        readySignal.await();
        startSignal.countDown();
        doneSignal.await();
        executorService.shutdown();

        String emailHash = emailHashEncoder.encode(request.email());

        // then
        assertAll(
            () -> assertThat(successCount.get()).isEqualTo(1),
            () -> assertThat(failCount.get()).isEqualTo(9),
            () -> assertThat(memberRepository.countByEmailHash(emailHash)).isEqualTo(1)
        );
    }

    /*
    로그인 메서드 테스트
     */
    @Test
    @DisplayName("로그인 테스트")
    void login() throws Exception {
        MemberSignUpRequest request = createSignUpRequest(
            "login@test.com",
            "password123!"
        );

        authService.signUp(request);

        LoginRequest loginRequest = new LoginRequest("login@test.com",
            "password123!");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.memberId()).isGreaterThan(0L);

        assertThat(response.email()).isEqualTo("login@test.com");
        assertThat(response.accessToken()).isNotNull();
        assertThat(userLoginHistoryRepository.countByMemberId(response.memberId())).isNotNull();
    }

    @Test
    @Disabled
    @DisplayName("로그인 실패 테스트 - email")
    void login_fail_userNotFound() {
        MemberSignUpRequest request = createSignUpRequest(
            "loginfail@test.com",
            "password123!"
        );

        authService.signUp(request);

        LoginRequest loginRequest = new LoginRequest("loginfaild@test.com",
            "password123!");

        AppException exception = assertThrows(
            AppException.class,
            () -> authService.login(loginRequest)
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.USER_NOT_FOUND);
        assertThat(userLoginHistoryRepository.countByLoginResultAndFailReason(
            LoginResult.FAILURE,
            LoginFailReason.INVALID_CREDENTIALS
        )).isEqualTo(1L);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - pw1")
    void login_fail_pw() {
        MemberSignUpRequest request = createSignUpRequest(
            "loginfailpw@test.com",
            "password123!"
        );

        authService.signUp(request);

        LoginRequest loginRequest = new LoginRequest("loginfailpw@test.com",
            "password123");

        AppException exception = assertThrows(
            AppException.class,
            () -> authService.login(loginRequest)
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("로그인 실패 테스트 - pw2")
    void login_fail_pw2() {
        MemberSignUpRequest request = createSignUpRequest(
            "signup@test.com",
            "password123!"
        );

        authService.signUp(request);

        LoginRequest loginRequest = new LoginRequest("signup@test.com",
            "password123");

        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            try {
                authService.login(loginRequest);
            } catch (AppException e) {
                if (e.getErrorCode().equals(MemberErrorCode.INVALID_PASSWORD)) {
                    count1.incrementAndGet();
                } else if (e.getErrorCode().equals(MemberErrorCode.LOGIN_TEMPORARILY_BLOCKED)) {
                    count2.incrementAndGet();
                }
            }
        }

        assertAll(
            () -> assertThat(count1.get()).isEqualTo(5),
            () -> assertThat(count2.get()).isEqualTo(5)
        );
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 회원 상태")
    void login_fail_memberStatus() {
        Member withdrawMember = MemberFixture.createMember(
            MemberStatus.WITHDRAWN,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member inactiveMember = MemberFixture.createMember(
            MemberStatus.INACTIVE,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member savedWithdrawMember = memberRepository.save(withdrawMember);
        Member savedInactiveMember = memberRepository.save(inactiveMember);

        LoginRequest withdrawRequest =
            new LoginRequest(dataEncryptor.decrypt(savedWithdrawMember.getEmailEncrypted()),
                "password123!");
        LoginRequest inactiveRequest =
            new LoginRequest(dataEncryptor.decrypt(savedInactiveMember.getEmailEncrypted()),
                "password123!");

        AppException withdrawException = assertThrows(AppException.class,
            () -> authService.login(withdrawRequest));
        AppException inactiveException = assertThrows(AppException.class,
            () -> authService.login(inactiveRequest));

        assertThat(withdrawException.getErrorCode()).isEqualTo(MemberErrorCode.WITHDRAWN_MEMBER);
        assertThat(inactiveException.getErrorCode()).isEqualTo(MemberErrorCode.INACTIVE_MEMBER);
    }

    @Test
    @DisplayName("판매자 회원 로그인")
    void seller_login() throws Exception {
        Member member = MemberFixture.createMember(
            MemberStatus.ACTIVE,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member member2 = MemberFixture.createMember(
            MemberStatus.ACTIVE,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        memberRepository.save(member);
        memberRepository.save(member2);

        Seller seller = SellerFixture.createSeller(
            member,
            SellerStatus.APPROVED,
            true,
            dataEncryptor
        );

        Seller seller2 = SellerFixture.createSeller(
            member2,
            SellerStatus.PENDING,
            false,
            dataEncryptor
        );

        Seller savedSeller = sellerRepository.save(seller);
        Seller savedSeller2 = sellerRepository.save(seller2);

        LoginRequest request = new LoginRequest(
            dataEncryptor.decrypt(member.getEmailEncrypted()),
            "password123!"
        );

        LoginRequest request2 = new LoginRequest(
            dataEncryptor.decrypt(member2.getEmailEncrypted()),
            "password123!"
        );

        LoginResponse response = authService.login(request);
        LoginResponse response2 = authService.login(request2);

        assertThat(response.memberId()).isNotNull();
        assertThat(response.accessToken()).isNotNull();
        assertThat(response2.memberId()).isNotNull();
        assertThat(response2.accessToken()).isNotNull();
    }

    /*
    계정 찾기 메서드
     */
    @Test
    @Disabled
    @DisplayName("계정찾기")
    void find_email() throws Exception {
        Member member = MemberFixture.createMember(
            MemberStatus.ACTIVE,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member savedMemver = memberRepository.save(member);

        AccountFindEmailRequest findEmailRequest = new AccountFindEmailRequest(
            "테스트회원",
            dataEncryptor.decrypt(savedMemver.getPhoneNumber())
        );

        AccountFindEmailResponse response = authService.findEmail(findEmailRequest);

        assertThat(response.email()).isEqualTo(
            dataEncryptor.decrypt(savedMemver.getEmailEncrypted()));
    }

    @Test
    @Disabled
    @DisplayName("계정찾기-실패")
    void find_email_fail() {
        MemberSignUpRequest request = createSignUpRequest(
            "findemailfail@test.com",
            "password123!"
        );

        authService.signUp(request);

        AccountFindEmailRequest findEmailRequest = new AccountFindEmailRequest(
            "홍길",
            "010-1234-5678"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.findEmail(findEmailRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    /*
    비밀번호 찾기 테스트
     */
    @Test
    @DisplayName("비밀번호 찾기")
    void find_pw() throws Exception {
        MemberSignUpRequest request = createSignUpRequest(
            "findpw@test.com",
            "password123!"
        );

        authService.signUp(request);

        PasswordFindRequest findRequest = new PasswordFindRequest(
            "findpw@test.com",
            "홍길동",
            "010-1234-5678",
            "newpassword123!"
        );

        authService.findPassword(findRequest);

        LoginRequest loginRequest = new LoginRequest(
            "findpw@test.com",
            "newpassword123!"
        );

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.email()).isEqualTo("findpw@test.com");
    }

    @Test
    @DisplayName("비밀번호 찾기 실패 - email")
    void find_pw_email_fail() {
        MemberSignUpRequest request = createSignUpRequest(
            "findpw@test.com",
            "password123!"
        );

        authService.signUp(request);

        PasswordFindRequest findRequest = new PasswordFindRequest(
            "findpwemailfail@test.com",
            "홍길동",
            "010-1234-5678",
            "newpassword123!"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.findPassword(findRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 찾기 실패 - 상태")
    void find_pw_status_fail() {
        Member withdrawMember = MemberFixture.createMember(
            MemberStatus.WITHDRAWN,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member inactiveMember = MemberFixture.createMember(
            MemberStatus.INACTIVE,
            Gender.MALE,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member savedWithdrawMember = memberRepository.save(withdrawMember);
        Member savedInactiveMember = memberRepository.save(inactiveMember);

        PasswordFindRequest findWithdrawRequest = new PasswordFindRequest(
            dataEncryptor.decrypt(savedWithdrawMember.getEmailEncrypted()),
            dataEncryptor.decrypt(savedWithdrawMember.getName()),
            dataEncryptor.decrypt(savedWithdrawMember.getPhoneNumber()),
            "password123!"
        );
        PasswordFindRequest findInactiveRequest = new PasswordFindRequest(
            dataEncryptor.decrypt(savedInactiveMember.getEmailEncrypted()),
            dataEncryptor.decrypt(savedInactiveMember.getName()),
            dataEncryptor.decrypt(savedInactiveMember.getPhoneNumber()),
            "password123!"
        );

        AppException exceptionWithdraw = assertThrows(AppException.class,
            () -> authService.findPassword(findWithdrawRequest));
        AppException exceptionInactive = assertThrows(AppException.class,
            () -> authService.findPassword(findInactiveRequest));

        assertThat(exceptionWithdraw.getErrorCode()).isEqualTo(MemberErrorCode.WITHDRAWN_MEMBER);
        assertThat(exceptionInactive.getErrorCode()).isEqualTo(MemberErrorCode.INACTIVE_MEMBER);
    }

    @Test
    @DisplayName("비밀번호 찾기 실패 - 다른 정보")
    void find_pw_other_fail() {
        MemberSignUpRequest request = createSignUpRequest(
            "findpwother@test.com",
            "password123!"
        );

        authService.signUp(request);

        PasswordFindRequest findRequest = new PasswordFindRequest(
            "findpwother@test.com",
            "홍길",
            "010-1234-5678",
            "newpassword123!"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.findPassword(findRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_INPUT_VALUE);
    }

    /*
    비밀번호 재설정
     */
    @Test
    @DisplayName("비밀번호 재설정")
    void reset_pw() throws Exception {
        CustomUserPrincipal principal = loginAsActiveMaleMember();

        PasswordResetRequest resetRequest = new PasswordResetRequest(
            "password123!",
            "newpassword123!"
        );

        authService.resetPassword(principal.getId(), resetRequest);

        Optional<Member> member = memberRepository.findById(principal.getId());

        LoginRequest loginRequest = new LoginRequest(
            dataEncryptor.decrypt(member.get().getEmailEncrypted()),
            "newpassword123!"
        );

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.memberId()).isEqualTo(principal.getId());
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - ID")
    void reset_pw_id_fail() {
        CustomUserPrincipal principal = loginAsActiveMaleMember();

        PasswordResetRequest resetRequest = new PasswordResetRequest(
            "password123!",
            "newpassword123!"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.resetPassword(100L, resetRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 기존 비밀번호 불일치")
    void reset_pw_pw_fail() {
        CustomUserPrincipal principal = loginAsActiveMaleMember();

        PasswordResetRequest resetRequest = new PasswordResetRequest(
            "password123",
            "newpassword123!"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.resetPassword(principal.getId(), resetRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_PASSWORD);
    }

    /*
    회원 탈퇴
     */
    @Test
    @DisplayName("회원 탈퇴")
    void member_withdraw() {
        CustomUserPrincipal principal = loginAsActiveMaleMember();

        WithDrawRequest withDrawRequest = new WithDrawRequest(
            "password123!"
        );

        authService.withdraw(principal.getId(), withDrawRequest);

        Optional<Member> member = memberRepository.findById(principal.getId());

        LoginRequest loginRequest = new LoginRequest(
            dataEncryptor.decrypt(member.get().getEmailEncrypted()),
            "password123!"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.login(loginRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.WITHDRAWN_MEMBER);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - ID")
    void member_withdraw_fail_id() {
        CustomUserPrincipal principal = loginAsActiveMaleMember();

        WithDrawRequest withDrawRequest = new WithDrawRequest(
            "password123!"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.withdraw(100L, withDrawRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - pw")
    void member_withdraw_fail_pw() {
        CustomUserPrincipal principal = loginAsActiveMaleMember();

        WithDrawRequest withDrawRequest = new WithDrawRequest(
            "password123"
        );

        AppException exception = assertThrows(AppException.class,
            () -> authService.withdraw(principal.getId(), withDrawRequest));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("회원가입, 로그인, 인증 접근, 권한 차단, 로그아웃 흐름 테스트")
    void authFlow_success() throws Exception {
        String email = "user1@test.com";
        String password = "password123!";

        signUp(email, password);

        String accessToken = loginAndGetToken(email, password);

        mockMvc.perform(get("/api/v1/test/protected"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/test/protected")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(post("/api/v1/test/seller-only")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("API 요청에 성공했습니다"))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("seller 권한이 있으면 seller-only API 접근 성공")
    void sellerOnly_success_withApprovedSellerPrincipal() throws Exception {
        CustomUserPrincipal seller = loginAsApprovedSeller();

        mockMvc.perform(post("/api/v1/test/seller-only")
                .with(user(seller)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_success() throws Exception {
        String email = "user-info@test.com";
        String password = "password123!";

        signUp(email, password);

        String accessToken = loginAndGetToken(email, password);

        mockMvc.perform(get("/api/v1/auth/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.name").value("홍길동"))
            .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
            .andExpect(jsonPath("$.data.birthday").value("1999-01-01"));
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 인증 토큰 없음")
    void getMyInfo_fail_withoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/users/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("내 정보 수정 성공")
    void updateMyInfo_success() throws Exception {
        String email = "user-update@test.com";
        String password = "password123!";

        signUp(email, password);

        String accessToken = loginAndGetToken(email, password);

        UserUpdateRequest request = new UserUpdateRequest(
            "김수정",
            "010-9999-8888",
            Gender.MALE,
            "2000-02-02"
        );

        mockMvc.perform(patch("/api/v1/auth/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.name").value("김수정"))
            .andExpect(jsonPath("$.data.phoneNumber").value("010-9999-8888"))
            .andExpect(jsonPath("$.data.birthday").value("2000-02-02"));
    }

    @Test
    @DisplayName("내 정보 수정 실패 - 인증 토큰 없음")
    void updateMyInfo_fail_withoutToken() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
            "김수정",
            "010-9999-8888",
            Gender.MALE,
            "2000-02-02"
        );

        mockMvc.perform(patch("/api/v1/auth/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("내 정보 수정 후 조회 시 수정된 정보가 반환된다")
    void updateMyInfo_thenGetMyInfo_success() throws Exception {
        String email = "user-update-check@test.com";
        String password = "password123!";

        signUp(email, password);

        String accessToken = loginAndGetToken(email, password);

        UserUpdateRequest request = new UserUpdateRequest(
            "김수정",
            "010-9999-8888",
            Gender.MALE,
            "2000-02-02"
        );

        mockMvc.perform(patch("/api/v1/auth/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(get("/api/v1/auth/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.name").value("김수정"))
            .andExpect(jsonPath("$.data.phoneNumber").value("010-9999-8888"))
            .andExpect(jsonPath("$.data.birthday").value("2000-02-02"));
    }

    private void signUp(String email, String password) throws Exception {
        MemberSignUpRequest request = createSignUpRequest(email, password);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        return root.path("data").path("accessToken").asString();
    }

    private MemberSignUpRequest createSignUpRequest(String email, String password) {
        return new MemberSignUpRequest(
            email,
            password,
            "홍길동",
            Gender.MALE,
            "1999-01-01",
            "010-1234-5678"
        );
    }

    private String toJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    @TestConfiguration
    static class TestControllerConfig {

        @RestController
        @RequestMapping("/api/v1/test")
        static class TestAuthController {

            @GetMapping("/protected")
            public ApiResponse<Void> protectedApi() {
                return ApiResponse.success();
            }

            @PostMapping("/seller-only")
            @PreAuthorize("@authChecker.canManageSellerProduct()")
            public ApiResponse<Void> sellerOnlyApi() {
                return ApiResponse.success();
            }

            @PatchMapping("/user-update")
            public ApiResponse<Void> userUpdateApi() {
                return ApiResponse.success();
            }
        }
    }
}