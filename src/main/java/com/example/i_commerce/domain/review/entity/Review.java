package com.example.i_commerce.domain.review.entity;

import com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "reviews")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderProductId;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer starRate;

    private Long reportCount;

    private Long likeCount;

    private Boolean isBest;

    private Boolean isUpdated;

    private boolean isExcluded;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReviewIsBestStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review")
    private List<ReviewReport> reports = new ArrayList<>();


    public void addImage(String imageUrl) {
        ReviewImage reviewImage = ReviewImage.builder()
            .imageUrl(imageUrl)
            .review(this)
            .build();
        this.images.add(reviewImage);
    }

    public void update(String newContent, Integer newStarRate, List<String> newImageUrls) {
        if (!Objects.equals(this.content, newContent)) {
            this.content = newContent;
            isUpdated = true;
        }
        if (!Objects.equals(this.starRate, newStarRate)) {
            this.starRate = newStarRate;
            isUpdated = true;
        }
        List<String> currentUrls = this.images.stream()
            .map(ReviewImage::getImageUrl)
            .toList();

        if (!Objects.equals(currentUrls, newImageUrls)) {
            this.images.clear();

            if (newImageUrls != null) {
                newImageUrls.forEach(this::addImage);
            }
            isUpdated = true;
        }

        if (isUpdated) {
            this.isUpdated = true;
        }
    }


    public double calculateRecommendationScore() {
        double score = 0;

        if (this.content != null) {
            score += Math.min(this.content.length() / 100.0, 1.0) * 30;
        }

        if (this.starRate != null) {
            score += (this.starRate / 5.0) * 20;
        }

        long count = (this.likeCount == null) ? 0L : this.likeCount;
        score += Math.min(count / 50.0, 1.0) * 40;

        if (this.images != null && !this.images.isEmpty()) {
            score += 10;
        }

        return score;
    }

    public void checkBestEligibility(double threshold) {
        if (this.isExcluded || this.status == ReviewIsBestStatus.BEST) {
            return;
        }

        double currentScore = this.calculateRecommendationScore();

        this.status = (currentScore >= threshold) ? ReviewIsBestStatus.CANDIDATE : ReviewIsBestStatus.NORMAL;
    }

    public void excludeFromBest() {
        this.isExcluded = true;
        this.status = ReviewIsBestStatus.NORMAL;
        this.isBest = false;
    }

    public void updateBestStatus(boolean isBest) {
        this.isBest = isBest;
        this.status = isBest ? ReviewIsBestStatus.BEST : ReviewIsBestStatus.CANDIDATE;
    }

    public void approveAsBest() {
        this.isBest = true;
        this.status = ReviewIsBestStatus.BEST;
        this.isExcluded = false;
    }

    public void cancelBestStatus() {
        this.isBest = false;
        this.status = ReviewIsBestStatus.CANDIDATE;
    }

    public void increaseLikeCount() {
        this.likeCount++;
        this.calculateRecommendationScore();
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount --;
            this.calculateRecommendationScore();
        }
    }

    public static Review from(CreateReviewRequest dto) {
        Review review = Review.builder()
            .orderProductId(dto.getOrderProductId())
            .userId(dto.getUserId())
            .content(dto.getContent())
            .starRate(dto.getStarRate())
            .likeCount(0L)
            .reportCount(0L)
            .isBest(false)
            .isUpdated(false)
            .build();

        if (dto.getImageUrls() != null) {
            dto.getImageUrls().forEach(review::addImage);
        }
        return review;
    }
}
