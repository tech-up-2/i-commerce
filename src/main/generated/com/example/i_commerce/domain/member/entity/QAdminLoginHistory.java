package com.example.i_commerce.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminLoginHistory is a Querydsl query type for AdminLoginHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminLoginHistory extends EntityPathBase<AdminLoginHistory> {

    private static final long serialVersionUID = -405959419L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminLoginHistory adminLoginHistory = new QAdminLoginHistory("adminLoginHistory");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final QAdmin admin;

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

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAdminLoginHistory(String variable) {
        this(AdminLoginHistory.class, forVariable(variable), INITS);
    }

    public QAdminLoginHistory(Path<? extends AdminLoginHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminLoginHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminLoginHistory(PathMetadata metadata, PathInits inits) {
        this(AdminLoginHistory.class, metadata, inits);
    }

    public QAdminLoginHistory(Class<? extends AdminLoginHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new QAdmin(forProperty("admin")) : null;
    }

}

