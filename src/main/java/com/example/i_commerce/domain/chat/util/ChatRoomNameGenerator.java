package com.example.i_commerce.domain.chat.util;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomNameGenerator {

    private final DataEncryptor dataEncryptor;
    //    static을 붙여서 계속 호출되는 메모리 공간을 한 곳만 사용하도록 설정
    private static final String PRIVATE_ROOM_NAME = "%s님이 요청한 %s님과의 채팅";
    private static final String PRODUCT_ROOM_NAME = "%s 상품 채팅방";
    public String getPrivateRoomName(String memberName, String otherMemberName) {
        return String.format(PRIVATE_ROOM_NAME, memberName, otherMemberName);
    }
    public String getProductRoomName(String productName){
        return String.format(PRODUCT_ROOM_NAME, productName);
    }


}

