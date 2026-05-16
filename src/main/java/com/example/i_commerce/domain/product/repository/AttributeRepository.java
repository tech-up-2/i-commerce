package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.Attribute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {

    boolean existsByKey(String key);

    @Query("SELECT a FROM Attribute a ORDER BY a.key ASC, a.value ASC")
    List<Attribute> findAllOrderedByKeyAndValue();
}
