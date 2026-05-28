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

    // 💡 금칙어를 새로 추가할 때, 기존에 저장된 "forbiddenWords" 캐시를 싹 비워줍니다.
    // 그래야 다음 리뷰 검증 때 DB에서 최신 금칙어 목록을 다시 캐싱해 옵니다!
    @CacheEvict(value = "forbiddenWords", allEntries = true)
    public void addForbiddenWord(String word) {
        forbiddenWordRepository.save(new ReviewForbiddenWord(word));
    }
}
