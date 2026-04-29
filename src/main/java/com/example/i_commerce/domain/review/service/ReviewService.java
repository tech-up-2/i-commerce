package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.repo.ReviewRepo;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepo reviewRepo;

    //리뷰 작성 후 상세 페이지 이동
    @Transactional
    public Long createReview(CreateReviewRequest dto) {
        //1. 별점 유효성 검사
        validateStarRating(dto.getStarRate());

        //1-1.이미 리뷰를 작성했는지 검증
        if (reviewRepo.existsByOrderProductIdAndUserId(dto.getOrderProductId(), dto.getUserId())) {
            throw new AppException(ErrorCode.ALREADY_REVIEWED);
        }

        //2. DTO -> 엔티티
        Review review = Review.builder()
            .orderProductId(dto.getOrderProductId())
            .userId(dto.getUserId())
            .content(dto.getContent())
            .starRate(dto.getStarRate())
            .imageUrl(dto.getImgUrl())
            .isBest(false)
            .build();

        //3. 저장
        Review savedReview = reviewRepo.save(review);

        //4. 생성된 ID 반환
        return savedReview.getId();
    }

    @Transactional
    public List<ReviewListResponse> viewReviewList(Long orderProductId) {
        //1. 리포지토리에서 엔티티 리스트 가져오기
        List<Review> reviews = reviewRepo.findAllByOrderProductIdAndDeletedAtIsNull(orderProductId);

        //2. 엔티티 리스트 -> DTO 리스트로 변환하기
        List<ReviewListResponse> responseDtoList = new ArrayList<>();

        for (Review review : reviews) {
            ReviewListResponse resDto = ReviewListResponse.builder()
                .reviewId(review.getId())
                .userId(review.getUserId())
                .content(review.getContent())
                .starRate(review.getStarRate())
                .isBest(review.getIsBest())
                .imageUrl(review.getImageUrl())
                .build();

            responseDtoList.add(resDto);
        }
        return responseDtoList;
    }

    @Transactional
    public ReviewResponse viewDetailReview(Long reviewId) {
        //1. 해당 리뷰가 존재하는지 확인
        Review review = getReviewOrThrow(reviewId);

        //2. Entity -> dto
        ReviewResponse dto = ReviewResponse.builder()
            .userId(review.getUserId())
            .content(review.getContent())
            .score(review.getStarRate())
            .imageUrl(review.getImageUrl())
            .isBest(review.getIsBest())
            .createdAt(review.getCreatedAt())
            .isUpdated(Boolean.TRUE.equals(review.getIsUpdated()))
            .build();

        //반환
        return dto;
    }

    @Transactional
    public Long editReview(Long reviewId, UpdateReviewRequest dto) {
        //1. 리뷰가 존재하는 지 확인
        Review review = getReviewOrThrow(reviewId);

        //2. 권한 체크
        validateAuthor(review, dto.getUserId());

        //3. 엔티티의 메서드를 호출해서 값 변경하기
        review.update(dto.getContent(), dto.getStarRate(), dto.getImageUrl());

        //4.반환
        return reviewId;
    }

    @Transactional
    public void deleteReview(Long userId,Long reviewId) {
        //1. 존재하는 리뷰인지 확인
        Review review = getReviewOrThrow(reviewId);

        //2.권한체크(작성자인지 확인)
        validateAuthor(review, userId);

        review.delete();
    }

    public List<ReviewListResponse> markAsBestReview(Long orderProductId) {
        //1. 해당 상품의 리뷰를 모두 가져옴
        List<Review> reviews = reviewRepo.findAllByOrderProductIdAndDeletedAtIsNull(orderProductId);

        //2. [정렬] 점수를 기준으로 내림차순 정렬
        reviews.sort(new Comparator<Review> () {

            @Override
            public int compare(Review r1, Review r2) {
                return Double.compare(r2.calculateRecommendationScore(), r1.calculateRecommendationScore());
            }
        });

        //3. 정렬된 엔티티 리스트 -> DTO 리스트
        List<ReviewListResponse> responseDtos = new ArrayList<>();

        int limit = Math.min(reviews.size(), 10);

        for (int i = 0; i < limit; i++) {
            Review review = reviews.get(i);
            responseDtos.add(ReviewListResponse.from(review));
        }
        return responseDtos;
    }

    private void validateStarRating(int starRate) {
        if (starRate < 1 || starRate > 5) {
            throw new AppException(ErrorCode.INVALID_STAR_RATING);
        }
    }

    private Review getReviewOrThrow(Long reviewId) {
        return reviewRepo.findById(reviewId)
        .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateAuthor(Review review, Long userId) {
        if (!review.getUserId().equals(userId)) {
            throw(new AppException(ErrorCode.INVALID_PERMISSION));
        }
    }
}
