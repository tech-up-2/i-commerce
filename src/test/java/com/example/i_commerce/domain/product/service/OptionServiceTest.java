package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.product.application.service.OptionService;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.entity.enums.OptionInputType;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.OptionFixture;
import com.example.i_commerce.domain.product.presentation.request.CreateOptionRequest;
import com.example.i_commerce.domain.product.presentation.response.OptionResponse;
import com.example.i_commerce.domain.product.repository.OptionRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Option Service Unit Test")
public class OptionServiceTest {

    @InjectMocks
    private OptionService optionService;

    @Mock
    private OptionRepository optionRepository;

    private CreateOptionRequest basicOptionCreateRequest() {
        return new CreateOptionRequest("new 옵션명", OptionInputType.RADIO);
    }

    @Nested
    @DisplayName("옵션 생성 테스트")
    class CreateOptionTest {

        @Test
        @DisplayName("옵션이 정상적으로 생성된다.")
        void createOption_success(){
            // given
            CreateOptionRequest request = basicOptionCreateRequest();
            given(optionRepository.existsByName(request.name())).willReturn(false);

            // when
            optionService.createOption(request);
            
            // then
            then(optionRepository)
                .should(times(1)).existsByName(request.name());
            then(optionRepository)
                .should(times(1)).save(any(Option.class));
        }
        
        @Test
        @DisplayName("이미 존재하는 옵션명으로 요청시 예외가 발생한다.")
        void createOption_fail_duplicateOptionName(){
            // given
            CreateOptionRequest request = basicOptionCreateRequest();
            given(optionRepository.existsByName(request.name())).willReturn(true);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                optionService.createOption(request)
            );

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.DUPLICATE_OPTION_NAME);

            then(optionRepository).should(never()).save(any(Option.class));
        }

    }

    @Nested
    @DisplayName("옵션 전체 조회 테스트")
    class GetAllOptionsTest {

        @Test
        @DisplayName("전체 옵션이 정상적으로 조회된다.")
        void getAllOptions_success(){
            // given
            List<Option> options = OptionFixture.defaultOptions();
            given(optionRepository.findAllOrderedByName()).willReturn(options);

            // when
            List<OptionResponse> result = optionService.getAllOptions();

            // then
            assertThat(result).hasSize(options.size());

            assertThat(result.getFirst().name())
                .isEqualTo(options.getFirst().getName());
            assertThat(result.getLast().name())
                .isEqualTo(options.getLast().getName());

            then(optionRepository)
                .should(times(1)).findAllOrderedByName();
        }

        @Test
        @DisplayName("옵션이 존재하지 않으면 빈 리스트를 반환한다.")
        void getAllOptions_success_emptyList(){
            // given
            given(optionRepository.findAllOrderedByName()).willReturn(List.of());

            // when
            List<OptionResponse> result = optionService.getAllOptions();

            // then
            assertThat(result).isEmpty();

            then(optionRepository)
                .should(times(1)).findAllOrderedByName();
        }

    }


    @Nested
    @DisplayName("옵션 삭제 테스트")
    class DeleteOptionTest {

        @Test
        @DisplayName("옵션이 정상적으로 삭제된다.")
        void deleteOption_success(){
            // given
            Long optionId = 1L;
            Option option = OptionFixture.defaultOption().build();
            given(optionRepository.findById(optionId)).willReturn(Optional.of(option));
                
            // when
            optionService.deleteOption(optionId);

            // then
            then(optionRepository)
                .should(times(1)).findById(optionId);
            then(optionRepository)
                .should(times(1)).delete(option);
        }
        
        @Test
        @DisplayName("존재하지 않는 옵션 ID로 요청시 예외가 발생한다.")
        void deleteOption_fail_optionNotFound(){
            // given
            Long optionId = 100L;
            given(optionRepository.findById(optionId)).willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                optionService.deleteOption(optionId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.OPTION_NOT_FOUND);
            then(optionRepository).should(never()).delete(any(Option.class));

        }

    }

}
