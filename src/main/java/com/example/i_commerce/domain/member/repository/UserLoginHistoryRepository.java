package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.UserLoginHistory;
import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {

    long countByLoginResultAndFailReason(
        LoginResult loginResult,
        LoginFailReason failReason
    );

    long countByMemberId(Long memberId);
}
