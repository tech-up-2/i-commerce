package com.example.i_commerce.domain.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReview is a Querydsl query type for Review
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReview extends EntityPathBase<Review> {

    private static final long serialVersionUID = -1710461621L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReview review = new QReview("review");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final EnumPath<com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus> bestStatus = createEnum("bestStatus", com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus.class);

    public final QReviewComment comment;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ReviewImage, QReviewImage> images = this.<ReviewImage, QReviewImage>createList("images", ReviewImage.class, QReviewImage.class, PathInits.DIRECT2);

    public final BooleanPath isBest = createBoolean("isBest");

    public final BooleanPath isExcluded = createBoolean("isExcluded");

    public final BooleanPath isUpdated = createBoolean("isUpdated");

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final ListPath<ReviewLike, QReviewLike> likes = this.<ReviewLike, QReviewLike>createList("likes", ReviewLike.class, QReviewLike.class, PathInits.DIRECT2);

    public final NumberPath<Long> orderProductId = createNumber("orderProductId", Long.class);

    public final NumberPath<Long> reportCount = createNumber("reportCount", Long.class);

    public final ListPath<ReviewReport, QReviewReport> reports = this.<ReviewReport, QReviewReport>createList("reports", ReviewReport.class, QReviewReport.class, PathInits.DIRECT2);

    public final EnumPath<com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus> reportStatus = createEnum("reportStatus", com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus.class);

    public final NumberPath<Integer> starRate = createNumber("starRate", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QReview(String variable) {
        this(Review.class, forVariable(variable), INITS);
    }

    public QReview(Path<? extends Review> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReview(PathMetadata metadata, PathInits inits) {
        this(Review.class, metadata, inits);
    }

    public QReview(Class<? extends Review> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.comment = inits.isInitialized("comment") ? new QReviewComment(forProperty("comment"), inits.get("comment")) : null;
    }

}

