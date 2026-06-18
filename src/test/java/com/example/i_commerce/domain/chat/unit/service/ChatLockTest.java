package com.example.i_commerce.domain.chat.unit.service;

import com.example.i_commerce.domain.chat.repository.ChatRoomRepository;
import com.example.i_commerce.domain.chat.service.ChatRoomService;
import com.example.i_commerce.domain.chat.unit.service.fixture.ChatMemberFixture;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
public class ChatLockTest {
    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @BeforeEach
    public void setUp() {
        // 실제 서비스 로직 내부에서 SecurityContextHolder를 참조하므로 가짜 세션 주입
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(ChatMemberFixture.createPrincipal(), null, List.of())
        );
    }
    @Test
    @DisplayName("1:1 채팅방 생성을 동시에 요청할 시 2개의 채팅방이 생성된다.")
    void check_PrivateRoom_NoLock() throws InterruptedException {
        Long otherMemberId = 2L;
        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount); // 동시성 테스트를 위해 동시에 실행될 수 있또록 도와주는 메서드

    }

}
