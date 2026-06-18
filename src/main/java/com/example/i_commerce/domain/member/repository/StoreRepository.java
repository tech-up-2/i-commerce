package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllBySellerIdAndDeletedAtIsNull(Long sellerId);

    Optional<Store> findByIdAndSellerIdAndDeletedAtIsNull(Long storeId, Long sellerId);

    Optional<Store> findByIdAndDeletedAtIsNull(Long storeId);

    long countBySellerIdAndDeletedAtIsNull(Long id);
}
