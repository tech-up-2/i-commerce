package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.CategoryOption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryOptionRepository extends JpaRepository<CategoryOption, Long> {

    Optional<CategoryOption> findByCategoryId(Long categoryId);

}
