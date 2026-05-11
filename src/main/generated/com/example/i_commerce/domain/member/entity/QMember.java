package com.example.i_commerce.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 742897743L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final ArrayPath<byte[], Byte> birthday = createArray("birthday", byte[].class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final ListPath<DeliveryAddress, QDeliveryAddress> deliveryAddresses = this.<DeliveryAddress, QDeliveryAddress>createList("deliveryAddresses", DeliveryAddress.class, QDeliveryAddress.class, PathInits.DIRECT2);

    public final ArrayPath<byte[], Byte> emailEncrypted = createArray("emailEncrypted", byte[].class);

    public final StringPath emailHash = createString("emailHash");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isSeller = createBoolean("isSeller");

    public final ListPath<UserLoginHistory, QUserLoginHistory> loginHistories = this.<UserLoginHistory, QUserLoginHistory>createList("loginHistories", UserLoginHistory.class, QUserLoginHistory.class, PathInits.DIRECT2);

    public final ArrayPath<byte[], Byte> name = createArray("name", byte[].class);

    public final StringPath password = createString("password");

    public final ArrayPath<byte[], Byte> phoneNumber = createArray("phoneNumber", byte[].class);

    public final NumberPath<Integer> point = createNumber("point", Integer.class);

    public final ListPath<PointHistory, QPointHistory> pointHistories = this.<PointHistory, QPointHistory>createList("pointHistories", PointHistory.class, QPointHistory.class, PathInits.DIRECT2);

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.MemberType> role = createEnum("role", com.example.i_commerce.domain.member.entity.enums.MemberType.class);

    public final QSeller seller;

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.Gender> sex = createEnum("sex", com.example.i_commerce.domain.member.entity.enums.Gender.class);

    public final EnumPath<com.example.i_commerce.domain.member.entity.enums.MemberStatus> status = createEnum("status", com.example.i_commerce.domain.member.entity.enums.MemberStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.seller = inits.isInitialized("seller") ? new QSeller(forProperty("seller"), inits.get("seller")) : null;
    }

}

