package com.example.i_commerce.domain.chat.repository;

import com.example.i_commerce.domain.chat.entity.ChatParticipant;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id WHERE cp1.member.id = :myId AND cp2.member.id = :otherMemberId AND cp1.chatRoom.isGroupChat = false")
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myId") Long myId, @Param("otherMemberId") Long otherMemberId);
}
