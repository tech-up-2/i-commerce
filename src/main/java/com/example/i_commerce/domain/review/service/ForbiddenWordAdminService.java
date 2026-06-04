package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.review.entity.ReviewForbiddenWord;
import com.example.i_commerce.domain.review.repository.ReviewForbiddenWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForbiddenWordAdminService {

    private final ReviewForbiddenWordRepository forbiddenWordRepository;

    @CacheEvict(value = "forbiddenWords", allEntries = true)
    public void addForbiddenWord(String word) {
        forbiddenWordRepository.save(new ReviewForbiddenWord(word));
    }
}
