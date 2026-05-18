package com.example.i_commerce;

import com.example.i_commerce.domain.member.service.admin.dto.BootstrapAdminProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableConfigurationProperties(BootstrapAdminProperties.class)//최초관리자
@EnableJpaAuditing //엔티티 객체 생성, 수정일자 관리
@SpringBootApplication
public class ICommerceApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
        SpringApplication.run(ICommerceApplication.class, args);
    }

}
