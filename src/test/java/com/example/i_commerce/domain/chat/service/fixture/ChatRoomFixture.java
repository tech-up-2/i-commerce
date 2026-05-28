package com.example.i_commerce.domain.chat.service.fixture;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;

public class ChatRoomFixture {
    public static ChatRoom createChatGroupRoom(Long id, String name, Long productId) {
        return ChatRoom.builder()
            .id(id)
            .isGroupChat(true)
            .name(name)
            .productId(productId)
            .build();
    }
    public static ChatRoom createChatPrivateRoom(Long id, String name) {
        return ChatRoom.builder()
            .id(id)
            .isGroupChat(false)
            .name(name)
            .build();
    }
}
