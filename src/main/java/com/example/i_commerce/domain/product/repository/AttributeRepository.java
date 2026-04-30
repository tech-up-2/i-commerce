package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {

}
