package com.example.i_commerce.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserLoginHistory is a Querydsl query type for UserLoginHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserLoginHistory extends EntityPathBase<UserLoginHistory> {

    private static final long serialVersionUID = -1170846773L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserLoginHistory userLoginHistory = new QUserLoginHistory("userLoginHistory");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.LoginFailReason> failReason = createEnum("failReason", com.example.i_commerce.domain.member.entity.enums.LoginFailReason.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ipAddress = createString("ipAddress");

    public final DateTimePath<java.time.LocalDateTime> loginAt = createDateTime("loginAt", java.time.LocalDateTime.class);

    public final BooleanPath loginResult = createBoolean("loginResult");

    public final DateTimePath<java.time.LocalDateTime> logoutAt = createDateTime("logoutAt", java.time.LocalDateTime.class);

    public final QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUserLoginHistory(String variable) {
        this(UserLoginHistory.class, forVariable(variable), INITS);
    }

    public QUserLoginHistory(Path<? extends UserLoginHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserLoginHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserLoginHistory(PathMetadata metadata, PathInits inits) {
        this(UserLoginHistory.class, metadata, inits);
    }

    public QUserLoginHistory(Class<? extends UserLoginHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member"), inits.get("member")) : null;
    }

}

