package com.example.i_commerce.domain.review.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.order.service.dto.OrderProductResponse;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewComment;
import com.example.i_commerce.domain.review.entity.ReviewImage;
import com.example.i_commerce.domain.review.entity.enums.ReviewStatus;
import com.example.i_commerce.domain.review.repository.ReviewCommentRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.repository.StarRateCountProjection;
import com.example.i_commerce.domain.review.service.dto.CommentResponse;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.ReviewStatsResponse;
import com.example.i_commerce.domain.review.service.dto.SearchReviewRequest;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
import com.example.i_commerce.global.common.response.SliceResponse;
import com.example.i_commerce.global.s3.service.S3ImageService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceUnitTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private OrderService orderService;

    @Mock
    private ReviewForbiddenWordValidator forbiddenWord;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private S3ImageService s3ImageService;

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private StoreService storeService;

    @Mock
    private ReviewCommentRepository reviewCommentRepo;

    @Test
    @DisplayName("OrderStatus가 COMPLETED인 사용자가 리뷰를 작성한다.")
    void createReview() {
        //given
        Long userId = 1L;
        Long orderProductId = 10L;
        Long productId = 20L;

        CreateReviewRequest request = new CreateReviewRequest("좋아요",5);

        given(reviewRepo.existsByUserIdAndOrderProductIdAndStatus(userId, orderProductId, ReviewStatus.ACTIVE)).willReturn(false);

        OrderProductResponse mockResponse = new OrderProductResponse(productId, userId, OrderStatus.COMPLETED);
        given(orderService.getOrderProductForReview(orderProductId)).willReturn(mockResponse);

        Review mockReview = Review.builder().id(userId).build();
        given(reviewRepo.save(any(Review.class))).willReturn(mockReview);

        MockMultipartFile mockFile = new MockMultipartFile(
            "imageFiles",
            "test-image.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
        );
        List<MultipartFile> imageFiles = List.of(mockFile);

        given(s3ImageService.uploadImage(any(MultipartFile.class), eq("reviews")))
            .willReturn("https://s3.test-url.com/reviews/test-image.jpg");

        //when
        Long resultReviewId = reviewService.createReview(orderProductId, userId, request, imageFiles);

        //then
        assertThat(resultReviewId).isEqualTo(mockReview.getId());
        verify(s3ImageService, times(1)).uploadImage(any(MultipartFile.class), eq("reviews"));
    }

    @Test
    @DisplayName("작성된 리뷰를 슬라이싱, 옵션 검색한다.")
    void searchReviews() {
        //given
        SearchReviewRequest request = new SearchReviewRequest("옵션1", "강아지", 5);
        Pageable pageable = PageRequest.of(0, 10);

        Review mockReview = Review.builder()
            .id(100L)
            .content("강아지가 정말 좋아해요!")
            .starRate(5)
            .build();

        boolean hasNext = false;
        Slice<Review> mockSlice = new SliceImpl<>(List.of(mockReview), pageable, hasNext);

        given(reviewRepo.searchReviews(
            request.getOptionName(),
            request.getKeyword(),
            request.getStarRate(),
            pageable
        )).willReturn(mockSlice);

        //when
        SliceResponse<ReviewResponse> result = reviewService.searchReviews(request, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);

        assertThat(result.content().get(0).getReviewId()).isEqualTo(100L);
        assertThat(result.hasNext()).isEqualTo(false);

        verify(reviewRepo, times(1)).searchReviews(
            request.getOptionName(),
            request.getKeyword(),
            request.getStarRate(),
            pageable
        );
    }

    @Test
    @DisplayName("리뷰 리스트 목록을 조회한다.")
    void viewReviewList() {
        //given
        Long productId = 10L;
        Pageable pageable = PageRequest.of(0,10);

        Review mockReview = Review.builder()
            .id(100L)
            .starRate(5)
            .content("좋아요")
            .build();

        boolean hasNext = false;
        Slice<Review> mockSlice = new SliceImpl<>(List.of(mockReview), pageable, hasNext);

        given(reviewRepo.findByProductIdAndStatus(productId, ReviewStatus.ACTIVE, pageable)).willReturn(mockSlice);

        //when
        SliceResponse<ReviewListResponse> result = reviewService.viewReviewList(productId, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);

        assertThat(result.content().get(0).getReviewId()).isEqualTo(100L);

        verify(reviewRepo, times(1)).findByProductIdAndStatus(productId, ReviewStatus.ACTIVE, pageable);
    }


    @Test
    @DisplayName("특정 상품의 리뷰 통계를 확인한다.")
    void getProductReviewStats() {
        //given
        Long productId = 10L;

        StarRateCountProjection proj5 = mock(StarRateCountProjection.class);
        given(proj5.getStarRate()).willReturn(5);
        given(proj5.getCount()).willReturn(2L);

        StarRateCountProjection proj2 = mock(StarRateCountProjection.class);
        given(proj2.getStarRate()).willReturn(2);
        given(proj2.getCount()).willReturn(5L);

        List<StarRateCountProjection> mockProjections = List.of(proj5, proj2);
        given(reviewRepo.getStarRateStats(productId)).willReturn(mockProjections);

        //when
        ReviewStatsResponse result = reviewService.getProductReviewStats(productId);

        //then
        assertThat(result).isNotNull();

        assertThat(result.getTotalReviewCount()).isEqualTo(7L);
        assertThat(result.getAverageStarRate()).isEqualTo(2.9);
        assertThat(result.getStarDetails()).hasSize(5);

        assertThat(result.getStarDetails().get(4).getCount()).isEqualTo(2L);
        assertThat(result.getStarDetails().get(1).getCount()).isEqualTo(5L);
        assertThat(result.getStarDetails().get(0).getCount()).isEqualTo(0L);

        verify(reviewRepo, times(1)).getStarRateStats(productId);
    }

    @Test
    @DisplayName("리뷰 상세 조회를 한다.")
    void viewDetailReview() {
        //given
        Long reviewId = 1L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(1L)
            .content("강아지가 쓰기 좋아요")
            .starRate(4)
            .build();

        ReviewComment parentComment = ReviewComment.builder()
            .id(10L)
            .userId(3L)
            .content("고양이도 쓰기 좋을까요")
            .parent(null)
            .build();

        ReviewComment childComment = ReviewComment.builder()
            .id(20L)
            .userId(4L)
            .content("1번에(고양이..) 대한 대댓글!")
            .parent(parentComment)
            .build();

        given(reviewRepo.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)).willReturn(Optional.of(mockReview));

        given(reviewCommentRepo.findByReviewId(reviewId))
            .willReturn(List.of(parentComment, childComment));

        //when
        ReviewResponse result = reviewService.viewDetailReview(reviewId);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(reviewId);
        assertThat(result.getContent()).isEqualTo("강아지가 쓰기 좋아요");
        assertThat(result.getStarRate()).isEqualTo(4);

        List<CommentResponse> comments = result.getComments();
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getCommentId()).isEqualTo(10L);

        assertThat(comments.get(0).getChildren()).hasSize(1);
        assertThat(comments.get(0).getChildren().get(0).getCommentId()).isEqualTo(20L);

        verify(reviewRepo, times(1)).findByIdAndStatus(reviewId, ReviewStatus.ACTIVE);
        verify(reviewCommentRepo, times(1)).findByReviewId(reviewId);

    }

    @Test
    @DisplayName("자신이 작성한 리뷰를 수정한다.")
    void editReview() {
        //given
        Long reviewId = 1L;
        Long userId = 10L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(userId)
            .content("원래내용")
            .starRate(5)
            .images(new ArrayList<>())
            .build();

        given(reviewRepo.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)).willReturn(Optional.of(mockReview));

        UpdateReviewRequest request = new UpdateReviewRequest(userId, "쓰다보니 불편해요", 2, null);

        MockMultipartFile mockFile = new MockMultipartFile(
            "imageFiles",
            "test-image.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
        );
        List<MultipartFile> imageFiles = List.of(mockFile);

        given(s3ImageService.uploadImage(any(MultipartFile.class), eq("reviews")))
            .willReturn("https://s3.rul/new-test-image.jpg");

        //when
        Long editReviewId = reviewService.editReview(reviewId, userId, request, imageFiles);

        //then
        assertThat(editReviewId).isEqualTo(reviewId);

        assertThat(mockReview.getContent()).isEqualTo("쓰다보니 불편해요");
        assertThat(mockReview.getStarRate()).isEqualTo(2);

        verify(s3ImageService, times(1)).uploadImage(any(MultipartFile.class), eq("reviews"));
    }

    @Test
    @DisplayName("자신이 작성한 리뷰를 삭제한다.")
    void deleteReview() {
        //given
        Long userId = 1L;
        Long reviewId = 10L;

        ReviewImage image1 = ReviewImage.builder().imageUrl("test1").build();
        ReviewImage image2 = ReviewImage.builder().imageUrl("test2").build();
        List<ReviewImage> mockImages = List.of(image1, image2);

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(userId)
            .content("너무 좋아요")
            .starRate(5)
            .status(ReviewStatus.ACTIVE)
            .images(mockImages)
            .build();

        given(reviewRepo.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)).willReturn(Optional.of(mockReview));

        //when
        reviewService.deleteReview(userId, reviewId);

        //then
        assertThat(mockReview.getStatus()).isEqualTo(ReviewStatus.DELETED);

        verify(s3ImageService, times(2)).deleteImage(anyString());
    }

    @Test
    @DisplayName("베스트 리뷰 후보를 확인한다.")
    void getBestReviewCandidates() {
        //given
        Long productId = 1L;
        Long sellerId = 10L;
        Long storeId = 100L;

        Review excludedReview = mock(Review.class);
        given(excludedReview.isExcluded()).willReturn(true);

        given(productQueryService.getStoreIdByProductId(productId)).willReturn(storeId);
        given(storeService.isStoreManager(sellerId, storeId)).willReturn(true);

        Review lowScoreReview = mock(Review.class);
        given(lowScoreReview.getId()).willReturn(100L);
        given(lowScoreReview.isExcluded()).willReturn(false);
        given(lowScoreReview.calculateRecommendationScore()).willReturn(10.0);

        Review highScoreReview = mock(Review.class);
        given(highScoreReview.getId()).willReturn(200L);
        given(highScoreReview.isExcluded()).willReturn(false);
        given(highScoreReview.calculateRecommendationScore()).willReturn(50.0);

        List<Review> reviews = new ArrayList<>(List.of(excludedReview, lowScoreReview, highScoreReview));
        given(reviewRepo.findAllByProductIdAndStatus(productId, ReviewStatus.ACTIVE)).willReturn(reviews);

        //when
        List<ReviewListResponse> result = reviewService.getBestReviewCandidates(productId, sellerId);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReviewId()).isEqualTo(200L);
        assertThat(result.get(1).getReviewId()).isEqualTo(100L);

        verify(reviewRepo, times(1)).findAllByProductIdAndStatus(productId, ReviewStatus.ACTIVE);
    }
}
