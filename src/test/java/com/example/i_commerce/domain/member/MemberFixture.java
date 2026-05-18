package com.example.i_commerce.domain.member;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MemberFixture {

    public static Member createMember(
        PasswordEncoder passwordEncoder,
        EmailHashEncoder emailHashEncoder,
        DataEncryptor dataEncryptor
    ) {
        return Member.builder()
            .emailHash(emailHashEncoder.encode("user@test.com"))
            .emailEncrypted(dataEncryptor.encrypt("user@test.com"))
            .password(passwordEncoder.encode("password123!"))
            .name(dataEncryptor.encrypt("홍길동"))
            .phoneNumber(dataEncryptor.encrypt("01012345678"))
            .birthday(dataEncryptor.encrypt("1999-01-01"))
            .sex(Gender.MALE)
            .role(MemberType.CUSTOMER)
            .status(MemberStatus.ACTIVE)
            .point(0)
            .isSeller(false)
            .build();
    }
}
