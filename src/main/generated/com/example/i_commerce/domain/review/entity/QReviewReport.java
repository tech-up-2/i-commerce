package com.example.i_commerce.domain.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewReport is a Querydsl query type for ReviewReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewReport extends EntityPathBase<ReviewReport> {

    private static final long serialVersionUID = -110409121L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewReport reviewReport = new QReviewReport("reviewReport");

    public final NumberPath<Long> adminId = createNumber("adminId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.example.i_commerce.domain.review.entity.enums.ReportProcessStatus> processStatus = createEnum("processStatus", com.example.i_commerce.domain.review.entity.enums.ReportProcessStatus.class);

    public final NumberPath<Long> reporterId = createNumber("reporterId", Long.class);

    public final StringPath reportReason = createString("reportReason");

    public final EnumPath<com.example.i_commerce.domain.review.entity.enums.ReportType> reportType = createEnum("reportType", com.example.i_commerce.domain.review.entity.enums.ReportType.class);

    public final QReview review;

    public final EnumPath<com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus> status = createEnum("status", com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus.class);

    public QReviewReport(String variable) {
        this(ReviewReport.class, forVariable(variable), INITS);
    }

    public QReviewReport(Path<? extends ReviewReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewReport(PathMetadata metadata, PathInits inits) {
        this(ReviewReport.class, metadata, inits);
    }

    public QReviewReport(Class<? extends ReviewReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QReview(forProperty("review"), inits.get("review")) : null;
    }

}

