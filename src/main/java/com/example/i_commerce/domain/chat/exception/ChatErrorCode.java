package com.example.i_commerce.domain.chat.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    // --- Chat 도메인(CHT) ---
    CANNOT_JOIN_PRIVATE_ROOM(HttpStatus.BAD_REQUEST, "CHT-40001", "1:1 채팅은 접속이 불가능합니다."),
    ONLY_GROUP_CHAT_CAN_LEAVE(HttpStatus.BAD_REQUEST, "CHT-40002", "그룹 채팅만 퇴장기능을 이용할 수 있습니다."),
    CHAT_ROOM_FORBIDDEN(HttpStatus.FORBIDDEN,"CHT-40301", "채팅방 접근 권한이 없습니다."),
    NOT_A_ROOM_MEMBER(HttpStatus.FORBIDDEN, "CHT-40302", "해당 방에 멤버가 아닙니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHT-40401", "해당 채팅방을 찾을 수 없습니다."),
    ALREADY_PARTICIPANT(HttpStatus.CONFLICT, "CHT-40901", "이미 채팅방에 참여 중인 유저입니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHT-40902", "이미 존재하는 채팅방입니다.");




    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
