package com.example.i_commerce.domain.review.integration;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.common.ReviewIntegrationTestSupport;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.global.s3.service.S3ImageService;
import com.example.i_commerce.global.security.checker.AuthChecker;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@AutoConfigureMockMvc
public class ReviewUploadIntegrationTest extends ReviewIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private S3ImageService s3ImageService;

    @MockitoBean
    private AuthChecker authChecker;

    @Test
    @DisplayName("[시나리오] 구매자가 주문 완료된 상품에 대해 별점, 내용, 이미지를 첨부하여 리뷰를 생성하면 성공한다.")
    void createReview() throws Exception {
        //given
        ReviewTestSet testSet = createReviewTestEnvironment();

        CustomUserPrincipal principal = new CustomUserPrincipal(
            PrincipalType.MEMBER,
            testSet.buyer().getId(),
            List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );

        CreateReviewRequest request = new CreateReviewRequest("캐리어가 튼튼해요", 5);

        MockMultipartFile requestPart = new MockMultipartFile(
            "review",
            "",
            org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
            "images",
            "travel_bag.jpg",
            org.springframework.http.MediaType.IMAGE_JPEG_VALUE,
            "fake-binary-image-content".getBytes()
        );

        given(s3ImageService.uploadImage(any(), any()))
            .willReturn("https://i-commerce-s3.com/reviews/travel_bag.jpg");

        given(authChecker.canWriteReviewAsMember()).willReturn(true);

        reviewRepository.deleteAllInBatch();

        //when
        mockMvc.perform(multipart("/api/v1/order-products/{orderProductId}/reviews", testSet.orderProduct().getId())
                .file(requestPart)
                .file(imagePart)
                .with(user(principal))
            )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("SUCCESS"));

        // then
        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(1);

        Review savedReview = reviews.get(0);
        assertThat(savedReview.getStarRate()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("캐리어가 튼튼해요");

        verify(s3ImageService, times(1)).uploadImage(any(), any());
    }
}
