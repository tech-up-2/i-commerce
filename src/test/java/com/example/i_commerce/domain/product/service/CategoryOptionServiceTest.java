package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.product.application.service.CategoryOptionService;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.CategoryOption;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.entity.enums.OptionInputType;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.CategoryFixture;
import com.example.i_commerce.domain.product.fixture.OptionFixture;
import com.example.i_commerce.domain.product.presentation.request.AddCategoryOptionRequest;
import com.example.i_commerce.domain.product.presentation.response.AddCategoryOptionResponse;
import com.example.i_commerce.domain.product.presentation.response.CategoryOptionResponse;
import com.example.i_commerce.domain.product.repository.CategoryOptionRepository;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.OptionRepository;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionKey;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryOption Service Unit Test")
public class CategoryOptionServiceTest {

    @InjectMocks
    private CategoryOptionService categoryOptionService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private CategoryOptionRepository categoryOptionRepository;


    @Nested
    @DisplayName("카테고리 제공 옵션 조회 테스트")
    class GetOptionsByCategoryTest {

        private CategoryOptionProjection defaultProjection() {
            return new CategoryOptionProjection(
                1L, false, 1L, "옵션명", OptionInputType.RADIO
            );
        }

        @Test
        @DisplayName("카테고리가 제공하는 옵션이 정상적으로 조회된다.")
        void getOptionsByCategory_success(){
            // given
            Long categoryId = 1L;
            List<CategoryOptionProjection> projections = List.of(defaultProjection());

            given(categoryRepository.existsById(categoryId))
                .willReturn(true);
            given(categoryOptionRepository.findOptionsByCategoryId(categoryId))
                .willReturn(projections);

            // when
            CategoryOptionResponse result =
                categoryOptionService.getOptionsByCategory(categoryId);
        
            // then
            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.options()).hasSize(projections.size());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 ID로 요청시 예외가 발생한다.")
        void getOptionsByCategory_fail_categoryNotFound(){
            // given
            Long categoryId = 999L;
            given(categoryRepository.existsById(categoryId))
                .willReturn(false);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryOptionService.getOptionsByCategory(categoryId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);
            then(categoryOptionRepository)
                .should(never()).findOptionsByCategoryId(anyLong());

        }

    }

    @Nested
    @DisplayName("카테고리 제공 옵션 추가 테스트")
    class AddCategoryOptionTest {

        @Captor
        private ArgumentCaptor<List<CategoryOption>> categoryOptionCaptor;

        private AddCategoryOptionRequest createRequest(
            List<Long> optionIds,
            boolean propagateToChildren
        ) {
            return new AddCategoryOptionRequest(
                optionIds, propagateToChildren, false
            );
        }

        @Test
        @DisplayName("카테고리 제공 옵션이 정상적으로 추가된다.")
        void addCategoryOption_success_withoutPropagation(){
            // given
            Long categoryId = 1L;
            List<Long> optionIds = List.of(1L, 2L);
            List<Long> targetCategoryIds = List.of(categoryId);
            List<Option> options = OptionFixture.defaultOptions();
            Category categoryRef = CategoryFixture.rootCategory();
            AddCategoryOptionRequest request = createRequest(optionIds, false);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(optionRepository.findAllById(optionIds)).willReturn(options);
            given(categoryOptionRepository.findExistingKeys(targetCategoryIds, optionIds))
                .willReturn(List.of());
            given(categoryRepository.getReferenceById(categoryId)).willReturn(categoryRef);

            // when
            AddCategoryOptionResponse result =
                categoryOptionService.addCategoryOptions(categoryId, request);

            // then
            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.skippedOptions()).isEmpty();
            then(categoryRepository).should(never()).findAllDescendantIds(any());

            then(categoryOptionRepository)
                .should(times(1)).saveAll(categoryOptionCaptor.capture());
            assertThat(categoryOptionCaptor.getValue()).hasSize(options.size());
        }

        @Test
        @DisplayName("카테고리 제공 옵션이 하위 카테고리에도 정상적으로 추가된다.")
        void addCategoryOption_success_withPropagation(){
            // given
            Long categoryId = 1L;
            List<Long> optionIds = List.of(1L, 2L);
            List<Long> targetCategoryIds = List.of(1L, 2L, 3L, 4L);
            List<Option> options = OptionFixture.defaultOptions();
            AddCategoryOptionRequest request = createRequest(optionIds, true);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(optionRepository.findAllById(optionIds)).willReturn(options);
            given(categoryRepository.findAllDescendantIds(categoryId))
                .willReturn(targetCategoryIds);
            given(categoryOptionRepository.findExistingKeys(targetCategoryIds, optionIds))
                .willReturn(List.of());
            targetCategoryIds.forEach(id ->
                given(categoryRepository.getReferenceById(id))
                    .willReturn(CategoryFixture.createRootWithId(id))
            );

            // when
            AddCategoryOptionResponse result =
                categoryOptionService.addCategoryOptions(categoryId, request);

            // then
            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.skippedOptions()).isEmpty();
            then(categoryRepository).should(times(1)).findAllDescendantIds(any());

            then(categoryOptionRepository)
                .should(times(1)).saveAll(categoryOptionCaptor.capture());
            assertThat(categoryOptionCaptor.getValue())
                .hasSize(targetCategoryIds.size() * options.size());
        }

        @Test
        @DisplayName("이미 제공되는 옵션이라면 저장되지 않고 정상적으로 스킵된다.")
        void addCategoryOption_success_existsSkip(){
            // given
            Long categoryId = 1L;
            List<Long> optionIds = List.of(1L, 2L);
            List<Option> options = List.of(
                OptionFixture.createWithIdAndName(1L, "색상"),
                OptionFixture.createWithIdAndName(2L, "사이즈")
            );
            List<CategoryOptionKey> existingKeys = List.of(
                new CategoryOptionKey(categoryId, 1L),
                new CategoryOptionKey(categoryId, 2L)
            );
            Category categoryRef = CategoryFixture.createRootWithId(categoryId);
            AddCategoryOptionRequest request = createRequest(optionIds, false);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(optionRepository.findAllById(optionIds)).willReturn(options);
            given(categoryOptionRepository.findExistingKeys(List.of(categoryId), optionIds))
                .willReturn(existingKeys);
            given(categoryRepository.getReferenceById(categoryId)).willReturn(categoryRef);


            // when
            AddCategoryOptionResponse result =
                categoryOptionService.addCategoryOptions(categoryId, request);

            // then
            assertThat(result.skippedOptions()).hasSize(existingKeys.size());

            then(categoryOptionRepository)
                .should().saveAll(categoryOptionCaptor.capture());
            assertThat(categoryOptionCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 ID로 요청시 예외가 발생한다.")
        void addCategoryOption_fail_categoryNotFound(){
            // given
            Long categoryId = 1L;
            AddCategoryOptionRequest request = createRequest(List.of(1L), true);
            given(categoryRepository.existsById(categoryId))
                .willReturn(false);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryOptionService.addCategoryOptions(categoryId, request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);
            then(categoryOptionRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("존재하지 않는 옵션 ID가 포함되면 예외가 발생한다.")
        void addCategoryOption_fail_optionNotFound(){
            // given
            Long categoryId = 1L;
            List<Long> optionIds = List.of(1L, 2L, 999L);
            List<Option> foundOptions = OptionFixture.defaultOptions();
            AddCategoryOptionRequest request = createRequest(optionIds, true);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(optionRepository.findAllById(optionIds)).willReturn(foundOptions);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryOptionService.addCategoryOptions(categoryId, request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.OPTION_NOT_FOUND);
            then(categoryOptionRepository).should(never()).saveAll(any());
        }

    }

    @Nested
    @DisplayName("카테고리 제공 옵션 삭제 테스트")
    class DeleteCategoryOptionTest {

        @Test
        @DisplayName("카테고리 제공 옵션을 정상적으로 삭제한다.")
        void deleteCategoryOption_success(){
            // given
            Long categoryId = 1L;
            Long categoryOptionId = 1L;
            CategoryOption categoryOption = mock(CategoryOption.class);

            given(categoryOptionRepository
                .findByIdAndCategoryId(categoryOptionId, categoryId))
                .willReturn(Optional.of(categoryOption));

            // when
            categoryOptionService.deleteCategoryOption(categoryId, categoryOptionId);

            // then
            then(categoryOptionRepository).should().delete(categoryOption);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 옵션 ID로 요청시 예외가 발생한다.")
        void deleteCategoryOption_fail_categoryOptionNotFound(){
            // given
            Long categoryId = 999L;
            Long categoryOptionId = 999L;

            given(categoryOptionRepository
                .findByIdAndCategoryId(categoryOptionId, categoryId))
                .willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryOptionService.deleteCategoryOption(categoryId, categoryOptionId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_OPTION_NOT_FOUND);
            then(categoryOptionRepository).should(never()).delete(any());
        }
        
    }

}
