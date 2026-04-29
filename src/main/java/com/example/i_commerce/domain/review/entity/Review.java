package com.example.i_commerce.domain.review.entity;

import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @Column(nullable = false, length = 50)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer starRate;

    private Long reportCount;

    private Long likeCount;

    private String imageUrl;

    private Boolean isBest = false;

    private Boolean isUpdated = false;


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


    public void update(String newContent, Integer newStarRate, String newImageUrl) {
        if (!Objects.equals(this.content, newContent)){
            this.content = newContent;
            isUpdated = true;
        }
        if (!Objects.equals(this.starRate, newStarRate)) {
            this.starRate = newStarRate;
            isUpdated = true;
        }
        if (!Objects.equals(this.imageUrl, newImageUrl)) {
            this.imageUrl = newImageUrl;
            isUpdated = true;
        }
    }

    public void updateBestStatus(boolean isBest) {
        this.isBest = isBest;
    }

    public double calculateRecommendationScore() {
        double score = 0;

        score += Math.min(this.content.length() / 100.0, 1.0) * 30;

        score += (this.starRate / 5.0) * 20;

        score += Math.min(this.likeCount / 50.0, 1) * 40;

        if (this.imageUrl != null && this.imageUrl.isEmpty()) {
            score += 10;
        }

        return score;
    }
}
