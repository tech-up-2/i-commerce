package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {

    // memberId로 활성 배송지 목록 조회
    @Query("""
        select d
        from DeliveryAddress d
        where d.memberId = :memberId
          and d.deletedAt is null
        order by d.isDefault desc, d.createdAt desc
        """)
    List<DeliveryAddress> findByMemberIdOrderByIsDefaultDescCreatedAtDesc(Long memberId);

    // 회원당 활성 배송지 개수 조회
    @Query("""
            select count(d)
            from DeliveryAddress d
            where d.memberId = :memberId
              and d.deletedAt is null
        """)
    long countByMemberId(Long memberId);//최대 갯수를 제어하기 위해 필요

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update DeliveryAddress d
            set d.isDefault = false
            where d.memberId = :memberId
              and d.isDefault = true
              and d.deletedAt is null
        """)
    void clearDefaultAddresses(Long memberId);//계정의 모든 배송지의 기본 배송지여부를 false로 바꿈

    //수정중인 배송지를 제외한 계정의 모든 배송지의 기본 배송지여부를 false로 바꿈
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update DeliveryAddress d
            set d.isDefault = false
            where d.memberId = :memberId
              and d.id != :excludeAddressId
              and d.isDefault = true
              and d.deletedAt is null
        """)
    void clearDefaultAddressesExcept(Long memberId, Long excludeAddressId);

    // 활성 배송지 단건 조회
    @Query("""
            select d
            from DeliveryAddress d
            where d.id = :id
              and d.memberId = :memberId
              and d.deletedAt is null
        """)
    Optional<DeliveryAddress> findByIdAndMemberId(Long id, Long memberId);
}
