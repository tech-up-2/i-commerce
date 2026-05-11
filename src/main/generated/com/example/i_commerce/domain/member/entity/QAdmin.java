package com.example.i_commerce.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdmin is a Querydsl query type for Admin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdmin extends EntityPathBase<Admin> {

    private static final long serialVersionUID = -679884038L;

    public static final QAdmin admin = new QAdmin("admin");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.AdminRole> adminRole = createEnum("adminRole", com.example.i_commerce.domain.member.entity.enums.AdminRole.class);

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.AdminStatus> adminStatus = createEnum("adminStatus", com.example.i_commerce.domain.member.entity.enums.AdminStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final ArrayPath<byte[], Byte> emailEncrypted = createArray("emailEncrypted", byte[].class);

    public final StringPath emailHash = createString("emailHash");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<AdminLoginHistory, QAdminLoginHistory> loginHistories = this.<AdminLoginHistory, QAdminLoginHistory>createList("loginHistories", AdminLoginHistory.class, QAdminLoginHistory.class, PathInits.DIRECT2);

    public final ArrayPath<byte[], Byte> name = createArray("name", byte[].class);

    public final StringPath password = createString("password");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAdmin(String variable) {
        super(Admin.class, forVariable(variable));
    }

    public QAdmin(Path<? extends Admin> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdmin(PathMetadata metadata) {
        super(Admin.class, metadata);
    }

}

