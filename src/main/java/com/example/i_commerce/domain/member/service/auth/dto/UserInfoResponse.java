package com.example.i_commerce.domain.member.service.auth.dto;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.tools.DataEncryptor;

public record UserInfoResponse(
    Long id,
    String email,
    String name,
    String phoneNumber,
    String birthday,
    MemberType role,
    MemberStatus status
) {

    public static UserInfoResponse from(Member member, DataEncryptor dataEncryptor) {
        return new UserInfoResponse(
            member.getId(),
            dataEncryptor.decrypt(member.getEmailEncrypted()),
            dataEncryptor.decrypt(member.getName()),
            dataEncryptor.decrypt(member.getPhoneNumber()),
            dataEncryptor.decrypt(member.getBirthday()),
            member.getRole(),
            member.getStatus()
        );
    }
}
