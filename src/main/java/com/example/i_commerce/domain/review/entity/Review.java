package com.example.i_commerce.domain.review.entity;

import com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus;
import com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus;
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
import jakarta.persistence.OneToOne;
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
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "reviews")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    private static final int REPORT_THRESHOLD = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderProductId;

    @Column(nullable = false)
    private Long productId;

    private String displayOptionName;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer starRate;

    @Builder.Default
    private Long reportCount = 0L;

    @Builder.Default
    private Long likeCount = 0L;

    @Builder.Default
    private Boolean isBest = false;

    @Builder.Default
    private Boolean isUpdated = false;

    @Builder.Default
    private boolean isExcluded = false;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewComment comment;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "best_status")
    private ReviewIsBestStatus bestStatus = ReviewIsBestStatus.NORMAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "report_status")
    private ReviewReportStatus reportStatus = ReviewReportStatus.NORMAL;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review")
    private List<ReviewReport> reports = new ArrayList<>();


    public void addImage(String imageUrl) {
        ReviewImage reviewImage = ReviewImage.builder()
            .imageUrl(imageUrl)
            .sortOrder(this.images.size() + 1)
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
        if (this.isExcluded || this.bestStatus == ReviewIsBestStatus.BEST) {
            return;
        }

        double currentScore = this.calculateRecommendationScore();

        this.bestStatus = (currentScore >= threshold) ? ReviewIsBestStatus.CANDIDATE : ReviewIsBestStatus.NORMAL;
    }

    public void excludeFromBest() {
        this.isExcluded = true;
        this.bestStatus = ReviewIsBestStatus.NORMAL;
        this.isBest = false;
    }

    public void updateBestStatus(boolean isBest) {
        this.isBest = isBest;
        this.bestStatus = isBest ? ReviewIsBestStatus.BEST : ReviewIsBestStatus.CANDIDATE;
    }

    public void approveAsBest() {
        this.isBest = true;
        this.bestStatus = ReviewIsBestStatus.BEST;
        this.isExcluded = false;
    }

    public void cancelBestStatus() {
        this.isBest = false;
        this.bestStatus = ReviewIsBestStatus.CANDIDATE;
        this.isExcluded = false;
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

    public void incrementReportCount() {
        if (this.reportCount == null) {
            this.reportCount = 0L;
        }

        this.reportCount++;

        if (this.reportCount >= REPORT_THRESHOLD && this.reportStatus != ReviewReportStatus.HIDDEN_PENDING) {
            this.reportStatus = ReviewReportStatus.HIDDEN_PENDING;
        }
    }

    public void resetReportCount() {
        this.reportCount = 0L;
    }

    public void updateStatus(ReviewReportStatus reviewReportStatus) {
        this.reportStatus = reviewReportStatus;
    }

    public static Review from(Long orderProductId, Long userId, Long productId, CreateReviewRequest dto) {
        Review review = Review.builder()
            .orderProductId(orderProductId)
            .userId(userId)
            .productId(productId)
            .content(dto.getContent())
            .starRate(dto.getStarRate())
            .likeCount(0L)
            .reportCount(0L)
            .bestStatus(ReviewIsBestStatus.NORMAL)
            .isBest(false)
            .isUpdated(false)
            .isExcluded(false)
            .images(new ArrayList<>())
            .build();

        return review;
    }
}
