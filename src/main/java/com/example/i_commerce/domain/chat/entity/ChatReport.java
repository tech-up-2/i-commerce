package com.example.i_commerce.domain.chat.entity;

import com.example.i_commerce.domain.chat.entity.enums.ChatReportReason;
import com.example.i_commerce.domain.chat.entity.enums.ChatReportStatus;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_reports")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(nullable = false)
    private Long reporterId;

    @Column(nullable = false)
    private Long reportedId;

    @Column(nullable = false)
    private String originalMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatReportReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatReportStatus status;

    public void updateStatus(ChatReportStatus status) {
        this.status = status;
    }
}
