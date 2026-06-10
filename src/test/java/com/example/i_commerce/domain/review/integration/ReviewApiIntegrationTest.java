package com.example.i_commerce.domain.review.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.common.ReviewIntegrationTestSupport;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.global.s3.service.S3ImageService;
import com.example.i_commerce.global.security.checker.AuthChecker;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class ReviewApiIntegrationTest extends ReviewIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthChecker authChecker;

    @MockitoBean
    private S3ImageService s3ImageService;

    @Test
    @DisplayName("인증된 사용자의 리뷰 생성, 목록 조회, 상세 조회, 수정, 삭제까지 검증한다.")
    void reviewTotalLifecycleScenario() throws Exception {
        ReviewTestSet testSet = createReviewTestEnvironment();

        CustomUserPrincipal testPrincipal = new CustomUserPrincipal(
            PrincipalType.MEMBER,
            testSet.buyer().getId(),
            List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );

        given(authChecker.canWriteReviewAsMember()).willReturn(true);
        given(authChecker.canDeleteReview()).willReturn(true);

        given(s3ImageService.uploadImage(any(), any()))
            .willReturn("https://i-commerce-s3.com/reviews/test.jpg");

        //리뷰 생성
        CreateReviewRequest createRequest = new CreateReviewRequest("캐리어가 튼튼해요", 5);

        MockMultipartFile requestPart = new MockMultipartFile(
            "review",
            "",
            org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(createRequest)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
            "images",
            "test.jpg",
            org.springframework.http.MediaType.IMAGE_JPEG_VALUE,
            "fake-image".getBytes()
        );

        MvcResult mvcResult = mockMvc.perform(multipart("/api/v1/order-products/{orderProductId}/reviews",
            testSet.orderProduct().getId())
            .file(requestPart)
            .file(imagePart)
            .with(user(testPrincipal))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);

        long generatedReviewId = root.get("data").asLong();

        //리뷰 목록 조회
        mockMvc.perform(get("/api/v1/reviews", testSet.product().getId())
                .param("productId", String.valueOf(testSet.product().getId()))
                .param("page", "0")
                .param("size", "10")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))

            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content[0].reviewId").value(generatedReviewId))
            .andExpect(jsonPath("$.data.content[0].content").value("캐리어가 튼튼해요"));

        //리뷰 세부 조회
        mockMvc.perform(get("/api/v1/reviews/{reviewId}", generatedReviewId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))

            .andExpect(jsonPath("$.data.content").value("캐리어가 튼튼해요"))
            .andExpect(jsonPath("$.data.starRate").value(5))

            .andExpect(jsonPath("$.data.imageUrls").isArray())
            .andExpect(jsonPath("$.data.imageUrls[0]").value("https://i-commerce-s3.com/reviews/test.jpg"));


        //리뷰 수정
        UpdateReviewRequest updateRequest = new UpdateReviewRequest(generatedReviewId, "캐리어가 크고 튼튼해요", 5, List.of());

        MockMultipartFile updateRequestPart = new MockMultipartFile(
            "review",
            "",
            org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(updateRequest)
        );

        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PATCH,"/api/v1/reviews/{reviewId}", generatedReviewId)
                .file(updateRequestPart)
                .with(user(testPrincipal))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));

        //리뷰 삭제
        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", generatedReviewId)
            .with(user(testPrincipal))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}", generatedReviewId)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound());
    }

}
