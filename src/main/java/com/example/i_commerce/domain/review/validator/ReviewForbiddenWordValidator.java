package com.example.i_commerce.domain.review.validator;

import com.example.i_commerce.domain.review.repository.ReviewForbiddenWordRepository;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewForbiddenWordValidator {

    private final ReviewForbiddenWordRepository reviewForbiddenWordRepository;

    public void validateContent(String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        String lowerContent = content.toLowerCase();
        List<String> forbiddenWords = reviewForbiddenWordRepository.findAllForbiddenWords();

        for (String word : forbiddenWords) {
            if (lowerContent.contains(word.toLowerCase())) {
                throw new AppException(ReviewErrorCode.FORBIDDEN_WORD_INCLUDED);
            }
        }
    }
}
