package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.AdminLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailBlacklistRepository extends JpaRepository<AdminLoginHistory, String> {

}
