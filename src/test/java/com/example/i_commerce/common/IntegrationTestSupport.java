package com.example.i_commerce.common;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected DataEncryptor dataEncryptor;
    @Autowired
    protected MemberRepository memberRepository;

    static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");

        postgresContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    protected CustomUserPrincipal loginAsMember() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        Member member = Member.builder()
                .name(dataEncryptor.encrypt("테스트회원"))
                .phoneNumber(dataEncryptor.encrypt("010-1234-5678"))
                .emailHash("hashedEmail" + uniqueId)
                .emailEncrypted(dataEncryptor.encrypt("test@example.com"))
                .password("password")
                .sex(Gender.MALE)
                .birthday(dataEncryptor.encrypt("20431123"))
                .build();
        memberRepository.save(member);

        return new CustomUserPrincipal(
                PrincipalType.MEMBER,
                member.getId(),
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }
}