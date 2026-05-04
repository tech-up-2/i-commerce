package com.example.i_commerce.domain.member.service;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.service.dto.AdminLoginResponse;
import com.example.i_commerce.domain.member.service.dto.LoginRequest;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
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
            .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), admin.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        String email = dataEncryptor.decrypt(admin.getEmailEncrypted());

        String accessToken = jwtTokenUtil.createToken(
            admin.getId(),
            email,
            admin.getAdminRole(),
            admin.getAdminStatus()
        );

        return new AdminLoginResponse(
            admin.getId(),
            accessToken
        );
    }

}
