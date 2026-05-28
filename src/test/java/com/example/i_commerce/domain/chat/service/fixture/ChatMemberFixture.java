package com.example.i_commerce.domain.chat.service.fixture;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;

public class ChatMemberFixture {
    public static CustomUserPrincipal createPrincipal() {
        return new CustomUserPrincipal(PrincipalType.MEMBER, 1L, "test1@naver.com", "1234", List.of());
    }
    public static Member createMember(Long id,String email) {

        return Member.builder()
            .id(id)
            .emailHash(email)
            .password("1234")
            .name("테스트 유저".getBytes())
            .sex(Gender.MALE)
            .birthday("20010101".getBytes())
            .phoneNumber("01012341234".getBytes())
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.CUSTOMER)
            .build();
    }
    public static Member createSeller(Long id,String email) {

        return Member.builder()
            .id(id)
            .emailHash(email)
            .password("1234")
            .name("테스트 판매자".getBytes())
            .sex(Gender.MALE)
            .birthday("20010202".getBytes())
            .phoneNumber("01043214321".getBytes())
            .point(0)
            .status(MemberStatus.ACTIVE)
            .role(MemberType.SELLER)
            .build();
    }

}
