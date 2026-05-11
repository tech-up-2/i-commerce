package com.example.i_commerce.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChatRoom is a Querydsl query type for ChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatRoom extends EntityPathBase<ChatRoom> {

    private static final long serialVersionUID = 833545478L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChatRoom chatRoom = new QChatRoom("chatRoom");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isGroupChat = createBoolean("isGroupChat");

    public final ListPath<ChatMessage, QChatMessage> messages = this.<ChatMessage, QChatMessage>createList("messages", ChatMessage.class, QChatMessage.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final ListPath<ChatParticipant, QChatParticipant> participants = this.<ChatParticipant, QChatParticipant>createList("participants", ChatParticipant.class, QChatParticipant.class, PathInits.DIRECT2);

    public final com.example.i_commerce.domain.product.entity.QProduct product;

    public final ListPath<MessageReadStatus, QMessageReadStatus> readStatuses = this.<MessageReadStatus, QMessageReadStatus>createList("readStatuses", MessageReadStatus.class, QMessageReadStatus.class, PathInits.DIRECT2);

    public final ListPath<ChatReport, QChatReport> reports = this.<ChatReport, QChatReport>createList("reports", ChatReport.class, QChatReport.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChatRoom(String variable) {
        this(ChatRoom.class, forVariable(variable), INITS);
    }

    public QChatRoom(Path<? extends ChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChatRoom(PathMetadata metadata, PathInits inits) {
        this(ChatRoom.class, metadata, inits);
    }

    public QChatRoom(Class<? extends ChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new com.example.i_commerce.domain.product.entity.QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

