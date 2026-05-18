package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.StoreAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreAddressRepository extends JpaRepository<StoreAddress, Long> {

    // 상점당 활성 주소 개수 조회
    long countByStoreIdAndDeletedAtIsNull(Long storeId);

    // storeId로 활성 주소 목록 조회
    @Query("""
        select d
        from StoreAddress d
        where d.storeId = :storeId
          and d.deletedAt is null
        order by d.isDefault desc, d.createdAt desc
        """)
    List<StoreAddress> findByStoreIdOrderByIsDefaultDescCreatedAtDesc(Long storeId);

    //주소찾기
    Optional<StoreAddress> findStoreAddressByIdAndStoreIdAndDeletedAtIsNull(Long id, Long storeId);

    // 기본주소 삭제 시 대체 기본주소로 지정할 활성 주소 조회
    Optional<StoreAddress> findFirstByStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long storeId);

    Optional<StoreAddress> findByStoreIdAndIsDefaultTrueAndDeletedAtIsNull(Long storeId);
}
