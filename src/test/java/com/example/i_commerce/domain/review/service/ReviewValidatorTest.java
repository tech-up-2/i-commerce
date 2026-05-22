package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewForbiddenWordRepository;
import com.example.i_commerce.domain.review.validator.ReviewValidator;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReviewValidatorTest {

    @Mock
    private ReviewForbiddenWordRepository reviewForbiddenWordRepository;

    @InjectMocks
    private ReviewValidator reviewValidator;

    @Test
    @DisplayName("리뷰 내용에 금칙어가 포함되어 있지 않으면 정상적으로 통과한다")
    void validateContent_success() {
        //given
        given(reviewForbiddenWordRepository.findAllWords())
            .willReturn(List.of("바보", "멍청이"));

        String cleanContent = "이 상품 정말 품질도 좋고 배송도 빠르네요! 추천합니다.";

        assertDoesNotThrow(() -> reviewValidator.validateContent(cleanContent));
    }

    @Test
    @DisplayName("리뷰 내용에 금칙어가 포함되어 있으면 AppException(FORBIDDEN_WORD_INCLUDED)이 발생한다")
    void validateContent_throwsException_whenForbiddenWordIncluded() {
        // given
        given(reviewForbiddenWordRepository.findAllWords())
            .willReturn(List.of("바보", "광고"));

        String badContent = "이 제품 완전 바보 같아요. 사지 마세요.";

        // when & then.
        assertThatThrownBy(() -> reviewValidator.validateContent(badContent))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ReviewErrorCode.FORBIDDEN_WORD_INCLUDED);
    }

    @Test
    @DisplayName("리뷰 내용이 null이거나 빈 문자열이면 검증을 건너뛴다")
    void validateContent_skip_whenContentIsNullOrEmpty() {
        // when & then
        assertDoesNotThrow(() -> reviewValidator.validateContent(""));
        assertDoesNotThrow(() -> reviewValidator.validateContent(null));
    }
}
