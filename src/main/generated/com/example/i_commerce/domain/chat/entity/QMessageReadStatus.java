package com.example.i_commerce.domain.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMessageReadStatus is a Querydsl query type for MessageReadStatus
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMessageReadStatus extends EntityPathBase<MessageReadStatus> {

    private static final long serialVersionUID = 1885216060L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMessageReadStatus messageReadStatus = new QMessageReadStatus("messageReadStatus");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final QChatMessage chatMessage;

    public final QChatRoom chatRoom;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRead = createBoolean("isRead");

    public final com.example.i_commerce.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMessageReadStatus(String variable) {
        this(MessageReadStatus.class, forVariable(variable), INITS);
    }

    public QMessageReadStatus(Path<? extends MessageReadStatus> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMessageReadStatus(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMessageReadStatus(PathMetadata metadata, PathInits inits) {
        this(MessageReadStatus.class, metadata, inits);
    }

    public QMessageReadStatus(Class<? extends MessageReadStatus> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.chatMessage = inits.isInitialized("chatMessage") ? new QChatMessage(forProperty("chatMessage"), inits.get("chatMessage")) : null;
        this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoom(forProperty("chatRoom"), inits.get("chatRoom")) : null;
        this.member = inits.isInitialized("member") ? new com.example.i_commerce.domain.member.entity.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

