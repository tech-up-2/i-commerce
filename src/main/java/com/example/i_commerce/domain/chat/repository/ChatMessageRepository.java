package com.example.i_commerce.domain.chat.repository;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    //오름차순으로 해당 채팅방의 채팅 내용을 정렬해서 보여주기 위해 선언
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc (ChatRoom chatRoom);
}
