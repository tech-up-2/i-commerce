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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ReviewComment parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<ReviewComment> children = new ArrayList<>();

    public void update(String newContent) {
        this.content = newContent;
    }

    public static ReviewComment of(Review review, Long userId, CreateCommentRequest request) {
        return ReviewComment.builder()
            .review(review)
            .userId(userId)
            .content(request.getContent())
            .build();
    }

    public static ReviewComment ofChild(Review review, Long userId, CreateCommentRequest request, ReviewComment parent) {
        return ReviewComment.builder()
            .review(review)
            .userId(userId)
            .content(request.getContent())
            .parent(parent)
            .build();
    }
}
