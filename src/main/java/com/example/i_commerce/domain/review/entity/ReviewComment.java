package com.example.i_commerce.domain.review.entity;


import com.example.i_commerce.domain.review.service.dto.CreateCommentRequest;
import com.example.i_commerce.domain.review.service.dto.UpdateCommentRequest;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_comments")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, length = 50)
    private Long sellerId;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void update(String newContent) {
        this.content = newContent;
    }

    public static ReviewComment of(Review review, Long sellerId, CreateCommentRequest request) {
        return ReviewComment.builder()
            .review(review)
            .sellerId(sellerId)
            .content(request.getContent())
            .build();
    }
}
