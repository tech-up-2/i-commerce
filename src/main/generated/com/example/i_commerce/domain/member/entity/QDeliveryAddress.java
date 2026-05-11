package com.example.i_commerce.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDeliveryAddress is a Querydsl query type for DeliveryAddress
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeliveryAddress extends EntityPathBase<DeliveryAddress> {

    private static final long serialVersionUID = -1201673397L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDeliveryAddress deliveryAddress = new QDeliveryAddress("deliveryAddress");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final ArrayPath<byte[], Byte> deliveryMemo = createArray("deliveryMemo", byte[].class);

    public final ArrayPath<byte[], Byte> detailAddress = createArray("detailAddress", byte[].class);

    public final ArrayPath<byte[], Byte> extraAddress = createArray("extraAddress", byte[].class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final ArrayPath<byte[], Byte> jibunAddress = createArray("jibunAddress", byte[].class);

    public final StringPath label = createString("label");

    public final QMember member;

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final ArrayPath<byte[], Byte> recipientName = createArray("recipientName", byte[].class);

    public final ArrayPath<byte[], Byte> recipientPhone = createArray("recipientPhone", byte[].class);

    public final ArrayPath<byte[], Byte> roadAddress = createArray("roadAddress", byte[].class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final ArrayPath<byte[], Byte> zipCode = createArray("zipCode", byte[].class);

    public QDeliveryAddress(String variable) {
        this(DeliveryAddress.class, forVariable(variable), INITS);
    }

    public QDeliveryAddress(Path<? extends DeliveryAddress> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDeliveryAddress(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDeliveryAddress(PathMetadata metadata, PathInits inits) {
        this(DeliveryAddress.class, metadata, inits);
    }

    public QDeliveryAddress(Class<? extends DeliveryAddress> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member"), inits.get("member")) : null;
    }

}

