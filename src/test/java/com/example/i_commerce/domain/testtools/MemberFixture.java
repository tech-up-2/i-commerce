package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MemberFixture {

    public static Member createMember(
        MemberStatus status,
        Gender gender,
        PasswordEncoder passwordEncoder,
        EmailHashEncoder emailHashEncoder,
        DataEncryptor dataEncryptor
    ) {
        String email = status.name().toLowerCase() + "-" + UUID.randomUUID() + "@test.com";

        return Member.builder()
            .emailHash(emailHashEncoder.encode(email))
            .emailEncrypted(dataEncryptor.encrypt(email))
            .password(passwordEncoder.encode("password123!"))
            .name(dataEncryptor.encrypt("테스트회원"))
            .phoneNumber(dataEncryptor.encrypt("010" + createRandomPhoneTail()))
            .birthday(dataEncryptor.encrypt("1999-01-01"))
            .sex(gender)
            .role(MemberType.CUSTOMER)
            .status(status)
            .point(0)
            .isSeller(false)
            .build();
    }

    public static Member createSellerMember(
        PasswordEncoder passwordEncoder,
        EmailHashEncoder emailHashEncoder,
        DataEncryptor dataEncryptor
    ) {
        String email = "seller-" + UUID.randomUUID() + "@test.com";

        return Member.builder()
            .emailHash(emailHashEncoder.encode(email))
            .emailEncrypted(dataEncryptor.encrypt(email))
            .password(passwordEncoder.encode("password123!"))
            .name(dataEncryptor.encrypt("판매자회원"))
            .phoneNumber(dataEncryptor.encrypt("010" + createRandomPhoneTail()))
            .birthday(dataEncryptor.encrypt("1999-01-01"))
            .sex(Gender.MALE)
            .role(MemberType.SELLER)
            .status(MemberStatus.ACTIVE)
            .point(0)
            .isSeller(true)
            .build();
    }

    private static String createRandomPhoneTail() {
        return String.format(
            "%08d",
            ThreadLocalRandom.current().nextInt(0, 100_000_000)
        );
    }
}
