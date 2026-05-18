package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.Option;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    boolean existsByName(String type);

    @Query("SELECT o FROM Option o ORDER BY o.name ASC")
    List<Option> findAllOrderedByName();

}
