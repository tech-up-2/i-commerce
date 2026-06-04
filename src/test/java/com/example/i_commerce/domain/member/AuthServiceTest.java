package com.example.i_commerce.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.service.auth.AuthService;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.auth.dto.MemberSignUpRequest;
import com.example.i_commerce.domain.member.service.auth.dto.SignUpResponse;
import com.example.i_commerce.domain.member.service.auth.dto.UserUpdateRequest;
import com.example.i_commerce.domain.testtools.IntegrationTestSupport;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
    @DisplayName("회원가입 실패")
    void signUp_fail() throws Exception {
        MemberSignUpRequest request = createSignUpRequest(
            "signuptest.com",
            "password123!"
        );

        SignUpResponse response = authService.signUp(request);

        //assertThat()
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