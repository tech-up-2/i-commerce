package com.example.i_commerce.domain.review.validator;

import com.example.i_commerce.domain.review.repo.ReviewForbiddenWordRepository;
import com.example.i_commerce.global.exception.AppException; // 여진님의 AppException 위치
import com.example.i_commerce.domain.review.exception.ReviewErrorCode; // 에러코드 위치
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewValidator {

    private final ReviewForbiddenWordRepository reviewForbiddenWordRepository;

    public void validateContent(String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        List<String> forbiddenWords = reviewForbiddenWordRepository.findAllWords();

        for (String word : forbiddenWords) {
            if (content.contains(word)) {
                throw new AppException(ReviewErrorCode.FORBIDDEN_WORD_INCLUDED);
            }
        }
    }
}
