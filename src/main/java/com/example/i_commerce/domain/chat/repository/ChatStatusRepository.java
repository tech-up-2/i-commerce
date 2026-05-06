package com.example.i_commerce.domain.chat.repository;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.entity.MessageReadStatus;
import com.example.i_commerce.domain.member.entity.Member;
import java.util.List;
import org.aspectj.bridge.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatStatusRepository extends JpaRepository<MessageReadStatus, Long> {
 List<MessageReadStatus> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
 List<MessageReadStatus> findByChatRoomAndMemberAndIsReadFalse(ChatRoom chatRoom, Member member);
 Long countByChatRoomAndMemberAndIsReadFalse(ChatRoom chatRoom, Member member);

}
