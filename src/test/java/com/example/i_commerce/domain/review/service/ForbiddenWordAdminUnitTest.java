package com.example.i_commerce.domain.review.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.review.entity.ReviewForbiddenWord;
import com.example.i_commerce.domain.review.repository.ReviewForbiddenWordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ForbiddenWordAdminUnitTest {

    @InjectMocks
    ForbiddenWordAdminService forbiddenService;

    @Mock
    ReviewForbiddenWordRepository forbiddenWordRepo;

    @Test
    @DisplayName("성공: 새로운 금지어를 추가하면 저장소의 save 메서드가 호출된다.")
    void addForbiddenWord() {
        // given
        String word = "비속어";

        // when
        forbiddenService.addForbiddenWord(word);

        //then
        verify(forbiddenWordRepo, times(1)).save(any(ReviewForbiddenWord.class));
    }
}
