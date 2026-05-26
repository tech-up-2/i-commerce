package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AdminFixture {

    public static Admin createAdmin(
        AdminRole adminRole,
        AdminStatus adminStatus,
        PasswordEncoder passwordEncoder,
        EmailHashEncoder emailHashEncoder,
        DataEncryptor dataEncryptor
    ) {
        String email = adminRole.name().toLowerCase() + adminStatus.name().toLowerCase() + "-"
            + UUID.randomUUID() + "@test.com";

        return Admin.builder()
            .emailHash(emailHashEncoder.encode(email))
            .emailEncrypted(dataEncryptor.encrypt(email))
            .password(passwordEncoder.encode("password123!"))
            .name(dataEncryptor.encrypt("관리자"))
            .adminRole(adminRole)
            .adminStatus(adminStatus)
            .build();
    }
}
