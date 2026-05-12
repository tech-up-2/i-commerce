package com.example.i_commerce.domain.chat.repository;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.MessageReadStatus;
import com.example.i_commerce.domain.chat.service.dto.MyChatListResponse;
import com.example.i_commerce.domain.member.entity.Member;
import java.util.List;
import org.aspectj.bridge.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    //    @Query("SELECT new com.example.i_commerce.domain.chat.service.dto.MyChatListResponse("
//        + "p.chatRoom.id, "
//        + "p.chatRoom.name, "
//        + "p.chatRoom.isGroupChat, "
//        + "(SELECT CAST(COUNT(m) AS long) FROM MessageReadStatus m "
//        + "WHERE m.chatRoom = p.chatRoom "
//        + "AND m.memberId = :memberId "
//        + "AND m.isRead = false) "
//        + " ) "
//        + "FROM ChatParticipant p "
//        + "WHERE p.memberId = :memberId")
//    DTO 프로젝션 방식의 N+1 해결책 방법을 선택해 해결에 사용!
    @Query("""
            SELECT new com.example.i_commerce.domain.chat.service.dto.MyChatListResponse(
                p.chatRoom.id,
                p.chatRoom.name,
                p.chatRoom.isGroupChat,
                (SELECT CAST(COUNT(m) AS long)
                 FROM MessageReadStatus m
                 WHERE m.chatRoom = p.chatRoom
                   AND m.memberId = :memberId
                   AND m.isRead = false)
            )
            FROM ChatParticipant p
            WHERE p.memberId = :memberId
        """)
    List<MyChatListResponse> findMyChatList(@Param("memberId") Long memberId);

    List<MessageReadStatus> findByChatRoomAndMemberId(ChatRoom chatRoom, Long memberId);

    List<MessageReadStatus> findByChatRoomAndMemberIdAndIsReadFalse(ChatRoom chatRoom,
        Long memberId);

    Long countByChatRoomAndMemberIdAndIsReadFalse(ChatRoom chatRoom, Long memberId);

}
