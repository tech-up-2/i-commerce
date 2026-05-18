package com.example.i_commerce.domain.review.repo;

import com.example.i_commerce.domain.review.entity.ReviewForbiddenWord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewForbiddenWordRepository extends JpaRepository<ReviewForbiddenWord, Long> {

    @Query("SELECT f.word FROM ReviewForbiddenWord f")
    List<String> findAllWords();
}
