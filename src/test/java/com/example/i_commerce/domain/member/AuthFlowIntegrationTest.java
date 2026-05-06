package com.example.i_commerce.domain.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.service.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.dto.MemberSignUpRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/test/seller-only")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("API 요청에 성공했습니다"))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {
        MemberSignUpRequest request = new MemberSignUpRequest(
            "signup@test.com",
            "password123!",
            "홍길동",
            Gender.MALE,
            "1999-01-01",
            "010-1234-5678"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.email").value("signup@test.com"));
    }

    private void signUp(String email, String password) throws Exception {
        MemberSignUpRequest request = new MemberSignUpRequest(
            email,
            password,
            "홍길동",
            Gender.MALE,
            "1999-01-01",
            "010-1234-5678"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);

        return root.path("data").path("accessToken").asText();
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
        }
    }
}
