package com.example.i_commerce.global.security.principal;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserPrincipal implements UserDetails {

    private final PrincipalType type;

    private final Long id;
    private final String email;
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(
        PrincipalType type,
        Long id,
        String email,
        String password,
        Collection<? extends GrantedAuthority> authorities
    ) {
        this.type = type;
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    //member 생성
    public static CustomUserPrincipal fromMember(Member member, Seller seller) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 기본 회원 권한
        authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));
        authorities.add(new SimpleGrantedAuthority("STATUS_" + member.getStatus().name()));

        // 판매자라면 추가
        if (seller != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
            authorities.add(
                new SimpleGrantedAuthority("SELLER_" + seller.getSellerStatus().name()));
        }

        return new CustomUserPrincipal(
            PrincipalType.MEMBER,
            member.getId(),
            member.getEmailHash(),
            member.getPassword(),
            authorities
        );
    }

    //admin생성
    public static CustomUserPrincipal fromAdmin(Admin admin) {

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("STATUS_" + admin.getAdminStatus().name()));
        authorities.add(new SimpleGrantedAuthority("ADMIN_" + admin.getAdminRole().name()));

        return new CustomUserPrincipal(
            PrincipalType.ADMIN,
            admin.getId(),
            admin.getEmail(),
            admin.getPassword(),
            authorities
        );
    }


    // =========================
    // Getter
    // =========================
    public Long getId() {
        return id;
    }

    public PrincipalType getType() {
        return type;
    }

    public boolean isMember() {
        return type == PrincipalType.MEMBER;
    }

    public boolean isAdmin() {
        return type == PrincipalType.ADMIN;
    }

    // =========================
    // UserDetails 구현
    // =========================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 탈퇴 계정 로그인 차단
     */
    @Override
    public boolean isEnabled() {
        return authorities.stream()
            .noneMatch(a -> a.getAuthority().equals("STATUS_WITHDRAWN"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 정지는 로그인 허용 (기능만 제한)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public enum PrincipalType {
        MEMBER,
        ADMIN
    }
}
