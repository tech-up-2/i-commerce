package com.example.i_commerce.domain.chat.util;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.global.exception.AppException;
import org.springframework.stereotype.Component;

@Component
public class ChatRoleChecker {
    public void roleCheck(Member member, Member otherMember) {
        if(member.getRole().equals(otherMember.getRole())){
            throw new AppException(ChatErrorCode.CANNOT_CHAT_SAME_ROLE);
        }

    }
}
