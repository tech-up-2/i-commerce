package com.example.i_commerce.domain.member.service;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.service.dto.AdminLoginResponse;
import com.example.i_commerce.domain.member.service.dto.LoginRequest;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.TokenPayload;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final DataEncryptor dataEncryptor;
    private final PasswordEncoder passwordEncoder;
    private final EmailHashEncoder emailHashEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Transactional(readOnly = true)
    public AdminLoginResponse login(LoginRequest dto) {
        Admin admin = adminRepository.findByEmailHash(emailHashEncoder.encode(dto.email()))
            .orElseThrow(() -> new AppException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), admin.getPassword())) {
            throw new AppException(MemberErrorCode.INVALID_PASSWORD);
        }

        validateLoginStatus(admin);// status상태 검즘

        String email = dataEncryptor.decrypt(admin.getEmailEncrypted());

        TokenPayload payload = new TokenPayload(
            PrincipalType.ADMIN,
            admin.getId(),
            email,
            admin.getAdminRole(),
            admin.getAdminStatus(),
            null
        );

        String accessToken = new JwtTokenUtil().createToken(payload);

        return new AdminLoginResponse(
            admin.getId(),
            accessToken
        );
    }

    private void validateLoginStatus(Admin admin) {
        switch (admin.getAdminStatus()) {
            case WITHDRAWN -> throw new AppException(MemberErrorCode.WITHDRAWN_MEMBER);
        }
    }

}
