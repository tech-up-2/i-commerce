package com.example.i_commerce.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatReport is a Querydsl query type for ChatReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatReport extends EntityPathBase<ChatReport> {

    private static final long serialVersionUID = -2130879841L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatReport chatReport = new QChatReport("chatReport");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final QChatMessage chatMessage;

    public final QChatRoom chatRoom;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath originalMessage = createString("originalMessage");

    public final EnumPath<com.example.i_commerce.domain.chat.entity.enums.ChatReportReason> reason = createEnum("reason", com.example.i_commerce.domain.chat.entity.enums.ChatReportReason.class);

    public final NumberPath<Long> reportedId = createNumber("reportedId", Long.class);

    public final NumberPath<Long> reporterId = createNumber("reporterId", Long.class);

    public final EnumPath<com.example.i_commerce.domain.chat.entity.enums.ChatReportStatus> status = createEnum("status", com.example.i_commerce.domain.chat.entity.enums.ChatReportStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatReport(String variable) {
        this(ChatReport.class, forVariable(variable), INITS);
    }

    public QChatReport(Path<? extends ChatReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatReport(PathMetadata metadata, PathInits inits) {
        this(ChatReport.class, metadata, inits);
    }

    public QChatReport(Class<? extends ChatReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatMessage = inits.isInitialized("chatMessage") ? new QChatMessage(forProperty("chatMessage"), inits.get("chatMessage")) : null;
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoom(forProperty("chatRoom"), inits.get("chatRoom")) : null;
    }

}

