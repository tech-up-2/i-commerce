package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findAllByProductId(Long productId);
}
