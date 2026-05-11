package com.example.i_commerce.domain.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReviewForbiddenWord is a Querydsl query type for ReviewForbiddenWord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewForbiddenWord extends EntityPathBase<ReviewForbiddenWord> {

    private static final long serialVersionUID = -727898344L;

    public static final QReviewForbiddenWord reviewForbiddenWord = new QReviewForbiddenWord("reviewForbiddenWord");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath word = createString("word");

    public QReviewForbiddenWord(String variable) {
        super(ReviewForbiddenWord.class, forVariable(variable));
    }

    public QReviewForbiddenWord(Path<? extends ReviewForbiddenWord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReviewForbiddenWord(PathMetadata metadata) {
        super(ReviewForbiddenWord.class, metadata);
    }

}

