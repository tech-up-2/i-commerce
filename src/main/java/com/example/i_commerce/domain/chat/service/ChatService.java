package com.example.i_commerce.domain.chat.service;

import com.example.i_commerce.domain.chat.repository.ChatMessageRepository;
import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;


}
