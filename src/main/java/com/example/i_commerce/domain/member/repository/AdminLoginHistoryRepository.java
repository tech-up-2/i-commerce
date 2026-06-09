package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.AdminLoginHistory;
import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLoginHistoryRepository extends JpaRepository<AdminLoginHistory, Long> {

    long countByLoginResultAndFailReason(
        LoginResult loginResult,
        LoginFailReason failReason
    );

    long countByAdminId(Long adminId);
}
