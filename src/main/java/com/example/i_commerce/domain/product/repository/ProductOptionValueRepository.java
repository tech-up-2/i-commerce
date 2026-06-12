package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {
    List<ProductOptionValue> findAllByProductId(Long productId);
}
