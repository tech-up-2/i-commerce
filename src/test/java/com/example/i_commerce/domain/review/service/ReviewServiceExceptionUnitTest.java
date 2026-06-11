package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.example.i_commerce.domain.review.entity.ReviewImage;
import com.example.i_commerce.domain.review.entity.enums.ReviewStatus;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceExceptionUnitTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ReviewForbiddenWordValidator reviewForbiddenWordValidator;

    @Mock
    private OrderService orderService;

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private StoreService storeService;


    @Test
    @DisplayName("이미 작성한 리뷰가 있음에도 리뷰 작성을 시도한다.")
    void alreadyReviewExist() {
        //given
        Long orderProductId = 1L;
        Long userId = 10L;

        CreateReviewRequest request = new CreateReviewRequest("그냥 그래요", 3);
        List<MultipartFile> imageFiles = new ArrayList<>();

        given(reviewRepo.existsByUserIdAndOrderProductIdAndStatus(userId, orderProductId, ReviewStatus.ACTIVE)).willReturn(true);

        //when
        AppException exception = assertThrows(AppException.class,
            () -> reviewService.createReview(orderProductId, userId, request, imageFiles));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ReviewErrorCode.ALREADY_REVIEWED);
        verify(reviewRepo, times(0)).save(any(Review.class));
    }

    @Test
    @DisplayName("제품을 구매하지 않았는데 리뷰 작성을 시도한다.")
    void notActualBuyer() {
        //given
        Long productId = 1L;
        Long orderProductId = 1L;
        Long realUserId = 10L;
        Long fakeUserId = 100L;

        CreateReviewRequest request = new CreateReviewRequest("그냥 그래요", 3);
        List<MultipartFile> imageFiles = new ArrayList<>();

        given(reviewRepo.existsByUserIdAndOrderProductIdAndStatus(fakeUserId, orderProductId, ReviewStatus.ACTIVE)).willReturn(false);

        OrderProductResponse orderInfo = new OrderProductResponse(productId, realUserId, OrderStatus.COMPLETED);

        given(orderService.getOrderProductForReview(orderProductId)).willReturn(orderInfo);

        //when
        AppException exception = assertThrows(AppException.class,
            () -> reviewService.createReview(orderProductId, fakeUserId, request, imageFiles));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ReviewErrorCode.NOT_ACTUAL_BUYER);
    }

    @Test
    @DisplayName("COMPLETED가 아닌 경우에 리뷰 작성을 시도한다.")
    void reviewNotAllowedStats() {
        //given
        Long orderProductId = 1L;
        Long productId = 1L;
        Long userId = 10L;

        CreateReviewRequest request = new CreateReviewRequest("그냥 그래요", 3);
        List<MultipartFile> imageFiles = new ArrayList<>();


        given(reviewRepo.existsByUserIdAndOrderProductIdAndStatus(userId, orderProductId, ReviewStatus.ACTIVE)).willReturn(false);

        OrderProductResponse orderInfo = new OrderProductResponse(productId, userId,OrderStatus.SHIPPING);

        given(orderService.getOrderProductForReview(orderProductId)).willReturn(orderInfo);

        //when
        AppException appException = assertThrows(AppException.class,
            () -> reviewService.createReview(orderProductId, userId, request, imageFiles));

        //then
        assertThat(appException.getErrorCode()).isEqualTo(ReviewErrorCode.REVIEW_NOT_ALLOWED_STATE);
    }

    @Test
    @DisplayName("기존 리뷰에 존재하지 않는 이미지 URL을 유지 목록에 포함시켜 수정 요청을 한다.")
    void unmatchedImage() {
        //given
        Long reviewId = 1L;
        Long userId = 10L;

        ReviewImage originalImage = ReviewImage.builder()
            .imageUrl("real.jpg")
            .build();

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(userId)
            .images(List.of(originalImage))
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        List<String> unmatchUrls = List.of(
            "real.jpg",
            "fake.jpg"
        );

        UpdateReviewRequest request = new UpdateReviewRequest(userId, "내용수정",3, unmatchUrls);
        List<MultipartFile> newImageFiles = new ArrayList<>();

        //when
        AppException exception = assertThrows(AppException.class,
            () -> reviewService.editReview(reviewId, userId, request, newImageFiles)
        );

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ReviewErrorCode.UNMATCHED_REVIEW_IMAGE);
    }

    @Test
    @DisplayName("기존 업로드 이미지와 추가 이미지의 합이 최대 업로드 가능 개수가 넘게 수정 요청을 한다.")
    void exceedMaxImage() {
        //given
        Long reviewId = 1L;
        Long userId = 10L;

        List<ReviewImage> originalImages = new ArrayList<>();
        List<String> keepUrls = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String url = "image" + i + ".jpg";
            originalImages.add(ReviewImage.builder().imageUrl(url).build());
            keepUrls.add(url);
        }

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(userId)
            .images(originalImages)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        List<MultipartFile> newImageFiles = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            MultipartFile mockFile = mock(MultipartFile.class);
            given(mockFile.isEmpty()).willReturn(false);
            newImageFiles.add(mockFile);
        }

        UpdateReviewRequest request = new UpdateReviewRequest(userId, "내용 수정", 5, keepUrls);

        //when
        AppException exception = assertThrows(AppException.class,
            () -> reviewService.editReview(reviewId, userId, request, newImageFiles)
        );

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ReviewErrorCode.EXCEED_MAX_IMAGE_COUNT);
    }

    @Test
    @DisplayName("별점 범위를 초과해 리뷰 작성을 시도한다.")
    void invalidStarRate() {
        //given
        Long orderProductId = 1L;
        Long userId = 10L;

        CreateReviewRequest request = new CreateReviewRequest("그냥 그래요", 7);
        List<MultipartFile> imageFiles = new ArrayList<>();

        //when
        AppException appException = assertThrows(AppException.class,
            () -> reviewService.createReview(orderProductId, userId, request, imageFiles));

        //then
        assertThat(appException.getErrorCode()).isEqualTo(ReviewErrorCode.INVALID_STAR_RATING);
    }

    @Test
    @DisplayName("리뷰를 작성한 사용자가 아닌데 수정을 시도한다.")
    void invalidPermissionEdit() {
        //given
        Long reviewId = 1L;
        Long authorId = 10L;
        Long fakeAuthorId = 99L;

        Review mockReview = Review.builder().
            id(reviewId)
            .userId(authorId)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        UpdateReviewRequest request = new UpdateReviewRequest(fakeAuthorId,"내용", 5, new ArrayList<>());

        // when
        AppException exception = assertThrows(AppException.class,
            () -> reviewService.editReview(reviewId, fakeAuthorId, request, new ArrayList<>())
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_PERMISSION);
    }

    @Test
    @DisplayName("유저 아이디가 null인 경우에 수정을 시도한다.")
    void nullUserEdit() {
        Long reviewId = 1L;
        Long userId = null;

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(99L)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        UpdateReviewRequest request = new UpdateReviewRequest(userId,"내용", 5, new ArrayList<>());

        // when
        AppException exception = assertThrows(AppException.class,
            () -> reviewService.editReview(reviewId, userId, request, new ArrayList<>())
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("실패: 자신의 상점이 아닌 다른 상점의 리뷰의 베스트 후보를 확인하려한다.")
    void notStoreManager() {
        // given
        Long hackerSellerId = 99L;
        Long productId = 100L;
        Long storeId = 500L;

        given(productQueryService.getStoreIdByProductId(productId)).willReturn(storeId);

        given(storeService.isStoreManager(hackerSellerId, storeId)).willReturn(false);

        // when
        AppException exception = assertThrows(AppException.class,
            () ->reviewService.getBestReviewCandidates(productId, hackerSellerId)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_PERMISSION);
    }
}
