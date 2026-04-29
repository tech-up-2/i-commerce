package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
