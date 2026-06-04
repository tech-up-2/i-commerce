package com.example.i_commerce.domain.chat.entity;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_message")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    //    강결합이 발생하므로 id만 받도록 수정하는 것이 맞음 (리펙토링 필요)
    @Column(nullable = false)
    private Long memberId;

    //    @Column(columnDefinition = "TEXT")
//    private String content;
//  TEXT를 500자로 제한하여 채팅 길이를 조절 추후 TEXT 방식을 채택할 수 있음.
    @Column(nullable = false, length = 500)
    private String content;

    @Builder.Default
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL)
    private List<MessageReadStatus> readStatuses = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatMessage")
    private List<ChatReport> reports = new ArrayList<>();

    @Builder.Default
    private boolean isBlind = false;

    @Builder.Default
    private boolean isDelete = false;

    public void blind(){
        this.isBlind =  true;
    }
}
