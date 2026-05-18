package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailHash(String emailHash);

    Optional<Member> findByEmailHashAndDeletedAtIsNull(String emailHash);

    List<Member> findAllByDeletedAtIsNull();//나중에 변경 필요

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);
}
