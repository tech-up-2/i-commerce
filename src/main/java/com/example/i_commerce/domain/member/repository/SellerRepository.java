package com.example.i_commerce.domain.member.repository;

import com.example.i_commerce.domain.member.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

}
