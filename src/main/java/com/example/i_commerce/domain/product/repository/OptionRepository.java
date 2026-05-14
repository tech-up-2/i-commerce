package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    boolean existsByType(String type);
}
