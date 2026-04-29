package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
}
