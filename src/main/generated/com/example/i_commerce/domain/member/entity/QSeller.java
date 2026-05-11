package com.example.i_commerce.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeller is a Querydsl query type for Seller
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeller extends EntityPathBase<Seller> {

    private static final long serialVersionUID = 914652468L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeller seller = new QSeller("seller");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> approvedAt = createDateTime("approvedAt", java.time.LocalDateTime.class);

    public final StringPath bankAccount = createString("bankAccount");

    public final StringPath bankName = createString("bankName");

    public final StringPath businessName = createString("businessName");

    public final StringPath businessNumber = createString("businessNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath depositorName = createString("depositorName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mailOrderRegistrationNumber = createString("mailOrderRegistrationNumber");

    public final QMember member;

    public final StringPath ownerName = createString("ownerName");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.SellerStatus> sellerStatus = createEnum("sellerStatus", com.example.i_commerce.domain.member.entity.enums.SellerStatus.class);

    public final ListPath<Store, QStore> stores = this.<Store, QStore>createList("stores", Store.class, QStore.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSeller(String variable) {
        this(Seller.class, forVariable(variable), INITS);
    }

    public QSeller(Path<? extends Seller> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeller(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeller(PathMetadata metadata, PathInits inits) {
        this(Seller.class, metadata, inits);
    }

    public QSeller(Class<? extends Seller> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member"), inits.get("member")) : null;
    }

}

