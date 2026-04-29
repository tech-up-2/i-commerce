package com.example.i_commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing //엔티티 객체 생성, 수정일자 관리
@SpringBootApplication
public class ICommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ICommerceApplication.class, args);
	}

}
