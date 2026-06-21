package com.example.i_commerce.domain.chat.unit.service.fixture;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ChatMemberFixture {
    public static CustomUserPrincipal createPrincipal() {
        return new CustomUserPrincipal(PrincipalType.MEMBER, 1L, List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
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
    public static Admin createAdmin(Long id,String email) {

        return Admin.builder()
            .id(id)
            .adminRole(AdminRole.MASTER)
            .adminStatus(AdminStatus.ACTIVE)
            .name("테스트 어드민".getBytes())
            .emailHash(email)
            .password("admin")
            .build();
    }

}
