package com.example.i_commerce.domain.member.service.admin;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.service.admin.dto.BootstrapAdminProperties;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//서버 시작 시 MASTER 관리자가 없을 때만 최초 관리자 1명 생성
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final BootstrapAdminProperties properties;
    private final AdminRepository adminRepository;
    private final EmailHashEncoder emailHashEncoder;
    private final DataEncryptor dataEncryptor;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }

        boolean masterExists = adminRepository.existsByAdminRole(AdminRole.MASTER);

        if (masterExists) {
            return;
        }

        String emailHash = emailHashEncoder.encode(properties.email());

        if (adminRepository.existsByEmailHash(emailHash)) {
            return;
        }

        Admin admin = Admin.builder()
            .emailHash(emailHash)
            .emailEncrypted(dataEncryptor.encrypt(properties.email()))
            .password(passwordEncoder.encode(properties.password()))
            .name(dataEncryptor.encrypt(properties.name()))
            .adminRole(AdminRole.MASTER)
            .adminStatus(AdminStatus.ACTIVE)
            .build();

        adminRepository.save(admin);
    }
}
