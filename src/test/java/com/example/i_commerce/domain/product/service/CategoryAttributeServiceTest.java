package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.product.application.dto.CategoryAttributeGroupDto;
import com.example.i_commerce.domain.product.application.mapper.CategoryAttributeMapper;
import com.example.i_commerce.domain.product.application.service.CategoryAttributeService;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.CategoryAttribute;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.AttributeFixture;
import com.example.i_commerce.domain.product.fixture.CategoryFixture;
import com.example.i_commerce.domain.product.presentation.request.AddCategoryAttributeRequest;
import com.example.i_commerce.domain.product.presentation.response.AddCategoryAttributeResponse;
import com.example.i_commerce.domain.product.presentation.response.CategoryAttributeResponse;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryAttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.projection.CategoryAttributeKey;
import com.example.i_commerce.domain.product.repository.projection.CategoryAttributeProjection;
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
@DisplayName("CategoryAttribute Service Unit Test")
public class CategoryAttributeServiceTest {

    @InjectMocks
    private CategoryAttributeService categoryAttributeService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private CategoryAttributeRepository categoryAttributeRepository;

    @Mock
    private CategoryAttributeMapper categoryAttributeMapper;


    @Nested
    @DisplayName("카테고리 제공 속성 조회 테스트")
    class GetAttributesByCategoryTest {
        
        @Test
        @DisplayName("카테고리가 제공하는 속성이 정상적으로 그룹화되어 조회된다.")
        void getAttributesByCategory_success(){
            // given
            Long categoryId = 1L;
            List<CategoryAttributeProjection> projections = List.of(
                new CategoryAttributeProjection(1L, false, 1L, "소재", "면") ,
                new CategoryAttributeProjection(2L, false, 1L, "소재", "면")
            );
            List<CategoryAttributeGroupDto> grouped = List.of(
                CategoryAttributeGroupDto.of("소재", List.of())
            );

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(categoryAttributeRepository.findWithAttributeByCategoryId(categoryId))
                .willReturn(projections);
            given(categoryAttributeMapper.toGroupResponse(projections))
                .willReturn(grouped);

            // when
            CategoryAttributeResponse result =
                categoryAttributeService.getAttributesByCategory(categoryId);


            // then
            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.attributes()).isEqualTo(grouped);
            then(categoryAttributeMapper).should(times(1)).toGroupResponse(projections);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 ID로 요청시 예외가 발생한다.")
        void getAttributesByCategory_fail_categoryNotFound(){
            // given
            Long categoryId = 999L;
            given(categoryRepository.existsById(categoryId)).willReturn(false);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryAttributeService.getAttributesByCategory(categoryId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);
            then(categoryAttributeRepository)
                .should(never()).findWithAttributeByCategoryId(any());
            then(categoryAttributeMapper)
                .should(never()).toGroupResponse(any());
        }

    }

    @Nested
    @DisplayName("카테고리 제공 속성 추가 테스트")
    class AddAttributeTest {

        @Captor
        private ArgumentCaptor<List<CategoryAttribute>> categoryAttrCaptor;

        private AddCategoryAttributeRequest createRequest(
            List<Long> attributeIds, Boolean propagate
        ) {
            return new AddCategoryAttributeRequest(
                attributeIds, propagate, false
            );
        }
        
        @Test
        @DisplayName("카테고리 제공 속성을 정상적으로 추가한다.")
        void addAttribute_success_withoutPropagation(){
            // given
            Long categoryId = 1L;
            List<Long> attributeIds = List.of(1L, 2L);
            List<Long> targetCategoryIds = List.of(categoryId);
            List<Attribute> attributes = AttributeFixture.defaultAttributes();
            Category categoryRef = CategoryFixture.rootCategory();

            AddCategoryAttributeRequest request = createRequest(attributeIds, false);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(attributeRepository.findAllById(attributeIds)).willReturn(attributes);
            given(categoryAttributeRepository.findExistingKeys(targetCategoryIds, attributeIds))
                .willReturn(List.of());
            given(categoryRepository.getReferenceById(categoryId)).willReturn(categoryRef);

            // when
            AddCategoryAttributeResponse result
                = categoryAttributeService.addCategoryAttribute(categoryId, request);

            // then
            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.skippedAttributes()).isEmpty();
            then(categoryRepository).should(never()).findAllDescendantIds(any());

            then(categoryAttributeRepository).should().saveAll(categoryAttrCaptor.capture());
            assertThat(categoryAttrCaptor.getValue()).hasSize(attributeIds.size());
        }

        @Test
        @DisplayName("카테고리 제공 속성이 하위 카테고리에도 정상적으로 추가된다.")
        void addAttribute_success_withPropagation(){
            // given
            Long categoryId = 1L;
            List<Long> attributeIds = List.of(1L, 2L);
            List<Long> targetCategoryIds = List.of(1L, 2L, 3L);
            List<Attribute> attributes = AttributeFixture.defaultAttributes();
            Category categoryRef = CategoryFixture.rootCategory();

            AddCategoryAttributeRequest request = createRequest(attributeIds, true);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(attributeRepository.findAllById(attributeIds)).willReturn(attributes);
            given(categoryRepository.findAllDescendantIds(categoryId))
                .willReturn(targetCategoryIds);
            given(categoryAttributeRepository.findExistingKeys(targetCategoryIds, attributeIds))
                .willReturn(List.of());
            given(categoryRepository.getReferenceById(categoryId)).willReturn(categoryRef);
            targetCategoryIds.forEach(id ->
                given(categoryRepository.getReferenceById(id))
                    .willReturn(CategoryFixture.createRootWithId(id))
            );

            // when
            AddCategoryAttributeResponse result
                = categoryAttributeService.addCategoryAttribute(categoryId, request);

            // then
            assertThat(result.categoryId()).isEqualTo(categoryId);
            assertThat(result.skippedAttributes()).isEmpty();
            then(categoryRepository).should().findAllDescendantIds(categoryId);

            then(categoryAttributeRepository).should().saveAll(categoryAttrCaptor.capture());
            assertThat(categoryAttrCaptor.getValue())
                .hasSize(targetCategoryIds.size() * attributes.size());
        }

        @Test
        @DisplayName("이미 제공되는 속성이라면 저장되지 않고 정상적으로 스킵된다.")
        void addAttribute_success_existsSkip(){
            // given
            Long categoryId = 1L;
            List<Long> attributeIds = List.of(1L, 2L);
            List<Attribute> attributes = List.of(
                AttributeFixture.createAttributeWithId(1L, "소재", "면"),
                AttributeFixture.createAttributeWithId(1L, "소재", "천")
            );
            List<CategoryAttributeKey> existingKeys = List.of(
                new CategoryAttributeKey(categoryId, 1L),
                new CategoryAttributeKey(categoryId, 2L)
            );
            Category categoryRef = CategoryFixture.rootCategory();
            AddCategoryAttributeRequest request = createRequest(attributeIds, false);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(attributeRepository.findAllById(attributeIds)).willReturn(attributes);
            given(categoryAttributeRepository
                .findExistingKeys(List.of(categoryId), attributeIds))
                .willReturn(existingKeys);
            given(categoryRepository.getReferenceById(categoryId)).willReturn(categoryRef);

            // when
            AddCategoryAttributeResponse result =
                categoryAttributeService.addCategoryAttribute(categoryId, request);

            // then
            assertThat(result.skippedAttributes()).hasSize(existingKeys.size());

            then(categoryAttributeRepository).should().saveAll(categoryAttrCaptor.capture());
            assertThat(categoryAttrCaptor.getValue()).isEmpty();

        }

        @Test
        @DisplayName("존재하지 않는 카테고리 ID로 요청시 예외가 발생한다.")
        void addAttribute_fail_categoryNotFound(){
            // given
            Long categoryId = 999L;
            List<Long> attributeIds = List.of(1L, 2L);
            AddCategoryAttributeRequest request = createRequest(attributeIds, false);

            given(categoryRepository.existsById(categoryId)).willReturn(false);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryAttributeService.addCategoryAttribute(categoryId, request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);
            then(categoryAttributeRepository).should(never()).saveAll(any());
        }

        @Test
        @DisplayName("존재하지 않는 속성 ID가 포함되면 예외가 발생한다.")
        void addAttribute_fail_attributeNotFound(){
            // given
            Long categoryId = 1L;
            List<Long> attributeIds = List.of(1L, 2L, 999L);
            List<Attribute> attributes = AttributeFixture.defaultAttributes();
            AddCategoryAttributeRequest request = createRequest(attributeIds, false);

            given(categoryRepository.existsById(categoryId)).willReturn(true);
            given(attributeRepository.findAllById(attributeIds)).willReturn(attributes);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryAttributeService.addCategoryAttribute(categoryId, request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.ATTRIBUTE_NOT_FOUND);
            then(categoryAttributeRepository).should(never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("카테고리 제공 속성 삭제 테스트")
    class DeleteAttributeTest {

        @Test
        @DisplayName("카테고리 제공 속성을 정상적으로 삭제한다.")
        void deleteAttribute_success(){
            // given
            Long categoryId = 1L;
            Long categoryAttributeId = 1L;
            CategoryAttribute categoryAttribute = mock(CategoryAttribute.class);

            given(categoryAttributeRepository
                .findByIdAndCategoryId(categoryAttributeId, categoryId))
                .willReturn(Optional.of(categoryAttribute));

            // when
            categoryAttributeService
                .deleteCategoryAttribute(categoryId, categoryAttributeId);
        
            // then
            then(categoryAttributeRepository).should().delete(categoryAttribute);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 속성 ID로 요청시 예외가 발생한다.")
        void deleteAttribute_fail_categoryAttributeNotFound(){
            // given
            Long categoryId = 1L;
            Long categoryAttributeId = 999L;

            given(categoryAttributeRepository
                .findByIdAndCategoryId(categoryAttributeId, categoryId))
                .willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryAttributeService
                    .deleteCategoryAttribute(categoryId, categoryAttributeId)
            );

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_ATTRIBUTE_NOT_FOUND);
            then(categoryAttributeRepository).should(never()).delete(any());
        }

    }


}
