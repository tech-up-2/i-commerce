package com.example.i_commerce.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = -908308223L;

    public static final QOrder order = new QOrder("order1");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final StringPath address = createString("address");

    public final StringPath addressDetail = createString("addressDetail");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final ListPath<Delivery, QDelivery> deliveries = this.<Delivery, QDelivery>createList("deliveries", Delivery.class, QDelivery.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<OrderProduct, QOrderProduct> orderProducts = this.<OrderProduct, QOrderProduct>createList("orderProducts", OrderProduct.class, QOrderProduct.class, PathInits.DIRECT2);

    public final EnumPath<com.example.i_commerce.domain.order.entity.emuns.OrderStatus> orderStatus = createEnum("orderStatus", com.example.i_commerce.domain.order.entity.emuns.OrderStatus.class);

    public final ListPath<Payment, QPayment> payments = this.<Payment, QPayment>createList("payments", Payment.class, QPayment.class, PathInits.DIRECT2);

    public final StringPath receiverName = createString("receiverName");

    public final StringPath receiverPhone = createString("receiverPhone");

    public final NumberPath<Integer> totalPayAmount = createNumber("totalPayAmount", Integer.class);

    public final NumberPath<Integer> totalProductAmount = createNumber("totalProductAmount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> usedPointAmount = createNumber("usedPointAmount", Integer.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath zipCode = createString("zipCode");

    public QOrder(String variable) {
        super(Order.class, forVariable(variable));
    }

    public QOrder(Path<? extends Order> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrder(PathMetadata metadata) {
        super(Order.class, metadata);
    }

}

