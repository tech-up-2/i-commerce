package com.example.i_commerce.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStockHistory is a Querydsl query type for StockHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStockHistory extends EntityPathBase<StockHistory> {

    private static final long serialVersionUID = -486548948L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStockHistory stockHistory = new QStockHistory("stockHistory");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final NumberPath<Integer> changeQuantity = createNumber("changeQuantity", Integer.class);

    public final EnumPath<StockChangeType> changeType = createEnum("changeType", StockChangeType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final QStock stock;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStockHistory(String variable) {
        this(StockHistory.class, forVariable(variable), INITS);
    }

    public QStockHistory(Path<? extends StockHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStockHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStockHistory(PathMetadata metadata, PathInits inits) {
        this(StockHistory.class, metadata, inits);
    }

    public QStockHistory(Class<? extends StockHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.stock = inits.isInitialized("stock") ? new QStock(forProperty("stock"), inits.get("stock")) : null;
    }

}

