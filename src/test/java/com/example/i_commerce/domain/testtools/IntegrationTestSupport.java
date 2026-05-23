package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

public class IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailHashEncoder emailHashEncoder;

    @Autowired
    private DataEncryptor dataEncryptor;

    public CustomUserPrincipal loginAsActiveMember() {
        return loginAsMember(MemberStatus.ACTIVE);
    }

    public CustomUserPrincipal loginAsSuspendedMember() {
        return loginAsMember(MemberStatus.SUSPENDED);
    }

    private CustomUserPrincipal loginAsMember(MemberStatus status) {
        Member member = MemberFixture.createMember(
            status,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member savedMember = memberRepository.save(member);

        return new CustomUserPrincipal(
            PrincipalType.MEMBER,
            savedMember.getId(),
            dataEncryptor.decrypt(savedMember.getEmailEncrypted()),
            null,
            List.of(
                new SimpleGrantedAuthority("ROLE_MEMBER"),
                new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                new SimpleGrantedAuthority("STATUS_" + status.name())
            )
        );
    }

    public CustomUserPrincipal loginAsPenddingSeller() {

    }
}
