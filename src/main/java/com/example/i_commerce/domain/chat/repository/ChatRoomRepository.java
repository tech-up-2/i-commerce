package com.example.i_commerce.domain.chat.repository;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomKey(String roomKey);
    boolean existsByProductIdAndIsGroupChat(Long productId, Boolean isGroupChat);
    List<ChatRoom> findByIsGroupChatTrueAndDeletedAtIsNull();

}
