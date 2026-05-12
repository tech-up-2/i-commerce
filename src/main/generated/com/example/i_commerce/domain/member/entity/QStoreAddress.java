package com.example.i_commerce.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoreAddress is a Querydsl query type for StoreAddress
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreAddress extends EntityPathBase<StoreAddress> {

    private static final long serialVersionUID = 224263624L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoreAddress storeAddress = new QStoreAddress("storeAddress");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final StringPath addressPhoneNumber = createString("addressPhoneNumber");

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.AddressType> addressType = createEnum("addressType", com.example.i_commerce.domain.member.entity.enums.AddressType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath detailAddress = createString("detailAddress");

    public final StringPath extraAddress = createString("extraAddress");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final StringPath jibunAddress = createString("jibunAddress");

    public final StringPath label = createString("label");

    public final StringPath roadAddress = createString("roadAddress");

    public final QStore store;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath zipCode = createString("zipCode");

    public QStoreAddress(String variable) {
        this(StoreAddress.class, forVariable(variable), INITS);
    }

    public QStoreAddress(Path<? extends StoreAddress> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoreAddress(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoreAddress(PathMetadata metadata, PathInits inits) {
        this(StoreAddress.class, metadata, inits);
    }

    public QStoreAddress(Class<? extends StoreAddress> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.store = inits.isInitialized("store") ? new QStore(forProperty("store"), inits.get("store")) : null;
    }

}

