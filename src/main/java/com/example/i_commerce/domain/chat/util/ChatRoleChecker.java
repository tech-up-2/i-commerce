package com.example.i_commerce.domain.chat.util;

import com.example.i_commerce.domain.chat.entity.ChatMessage;
import com.example.i_commerce.domain.chat.exception.ChatErrorCode;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.global.exception.AppException;
import org.springframework.stereotype.Component;

@Component
public class ChatRoleChecker {
    public void roleCheck(MemberChatInfo member, MemberChatInfo otherMember) {
        if(member.role().equals(otherMember.role())){
            throw new AppException(ChatErrorCode.CANNOT_CHAT_SAME_ROLE);
        }

    }
    public void mDelRoleChecker(MemberChatInfo member, ChatMessage chatMessage){
        if(!member.id().equals(chatMessage.getMemberId())){
            throw new AppException(ChatErrorCode.UNAUTHORIZED_DELETE);
        }
    }
}
