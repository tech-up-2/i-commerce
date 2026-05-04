package com.example.i_commerce.domain.chat.repository;

import com.example.i_commerce.domain.chat.entity.MessageReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatStatusRepository extends JpaRepository<MessageReadStatus, Long> {

}
