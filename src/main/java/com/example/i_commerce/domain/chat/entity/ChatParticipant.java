package com.example.i_commerce.domain.chat.entity;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity


/* 동시성 및 중복참여 방지
   chatRoom_Id에 특정 member_Id가 중복으로 참여하는 것을 DB Unique조건을
   사용하여 차단합니다. */
@Table(name = "chat_participants",
    uniqueConstraints = {@UniqueConstraint(
        name = "uk_chatroom_member",
        columnNames = {"chat_room_id","member_id"}
    )}
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    private Boolean isBan;
}
