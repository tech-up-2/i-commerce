package com.example.i_commerce.global.security.principal;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.global.security.SecurityAuthority;
import com.example.i_commerce.global.security.jwt.TokenPayload;
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

    public static CustomUserPrincipal fromTokenPayload(TokenPayload payload) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (payload.principalType() == PrincipalType.MEMBER) {
            MemberType role = (MemberType) payload.role();

            authorities.add(new SimpleGrantedAuthority(SecurityAuthority.ROLE_MEMBER));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
            authorities.add(
                new SimpleGrantedAuthority("STATUS_" + payload.accountStatus().getAuthority()));

            if (role == MemberType.SELLER && payload.sellerStatus() != null) {
                authorities.add(
                    new SimpleGrantedAuthority("SELLER_" + payload.sellerStatus().getAuthority()));
            }
        }

        if (payload.principalType() == PrincipalType.ADMIN) {
            AdminRole role = (AdminRole) payload.role();

            authorities.add(new SimpleGrantedAuthority(SecurityAuthority.ROLE_ADMIN));
            authorities.add(
                new SimpleGrantedAuthority("STATUS_" + payload.accountStatus().getAuthority()));
            authorities.add(new SimpleGrantedAuthority("ADMIN_" + role.name()));
        }

        return new CustomUserPrincipal(
            payload.principalType(),
            payload.accountId(),
            null,
            null,
            authorities
        );
    }

    public static CustomUserPrincipal fromMember(Member member, Seller seller) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(SecurityAuthority.ROLE_MEMBER));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + member.getRole().getAuthority()));
        authorities.add(new SimpleGrantedAuthority("STATUS_" + member.getStatus().name()));

        if (seller != null) {
            authorities.add(new SimpleGrantedAuthority(SecurityAuthority.ROLE_SELLER));
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

    public static CustomUserPrincipal fromAdmin(Admin admin) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(SecurityAuthority.ROLE_ADMIN));
        authorities.add(new SimpleGrantedAuthority("STATUS_" + admin.getAdminStatus().name()));
        authorities.add(new SimpleGrantedAuthority("ADMIN_" + admin.getAdminRole().name()));

        return new CustomUserPrincipal(
            PrincipalType.ADMIN,
            admin.getId(),
            admin.getEmailHash(),
            admin.getPassword(),
            authorities
        );
    }

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
        return null;
    }

    @Override
    public boolean isEnabled() {
        return authorities.stream()
            .noneMatch(a
                -> a.getAuthority().equals(SecurityAuthority.STATUS_WITHDRAWN));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public enum PrincipalType {MEMBER, ADMIN}
}