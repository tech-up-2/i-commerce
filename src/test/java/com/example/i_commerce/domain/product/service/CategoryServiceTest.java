package com.example.i_commerce.domain.product.service;


import static com.example.i_commerce.domain.product.entity.policy.CategoryPolicy.DEFAULT_TREE_DEPTH;
import static com.example.i_commerce.domain.product.entity.policy.CategoryPolicy.RECURSIVE_DEPTH_LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.product.application.mapper.CategoryMapper;
import com.example.i_commerce.domain.product.application.service.CategoryService;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.CategoryFixture;
import com.example.i_commerce.domain.product.presentation.request.CreateCategoryRequest;
import com.example.i_commerce.domain.product.presentation.response.CategoryResponse;
import com.example.i_commerce.domain.product.presentation.response.CreateCategoryResponse;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.projection.CategoryTreeRow;
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
@DisplayName("Category Service Unit Test")
public class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;


    @Nested
    @DisplayName("전체 카테고리 트리 조회 테스트")
    class GetAllCategoriesTest {

        @Test
        @DisplayName("요청된 깊이가 없을시 기본 깊이로 조회한다.")
        void getAllCategories_success_withNullRequestDepth(){
            // given
            List<CategoryTreeRow> rows = List.of(mock(CategoryTreeRow.class));
            List<CategoryResponse> responses = List.of(mock(CategoryResponse.class));

            given(categoryRepository.findAllCategoryTree(DEFAULT_TREE_DEPTH))
                .willReturn(rows);
            given(categoryMapper.toHierarchy(rows)).willReturn(responses);

            // when
            List<CategoryResponse> result = categoryService.getAllCategories(null);

            // then
            assertThat(result).isEqualTo(responses);
            then(categoryRepository).should(times(1)).findAllCategoryTree(DEFAULT_TREE_DEPTH);
        }

        @Test
        @DisplayName("요청된 깊이가 상한선을 초과하지 않았다면 요청된 깊이로 조회한다.")
        void getAllCategories_success_withRequestDepth(){
            // given
            int requestDepth = RECURSIVE_DEPTH_LIMIT - 1;
            List<CategoryTreeRow> rows = List.of(mock(CategoryTreeRow.class));
            List<CategoryResponse> responses = List.of(mock(CategoryResponse.class));
            
            given(categoryRepository.findAllCategoryTree(requestDepth))
                .willReturn(rows);
            given(categoryMapper.toHierarchy(rows)).willReturn(responses);

            // when
            List<CategoryResponse> result = categoryService.getAllCategories(requestDepth);

            // then
            assertThat(result).isEqualTo(responses);
            then(categoryRepository).should(times(1)).findAllCategoryTree(requestDepth);
        }
        
        @Test
        @DisplayName("요청된 깊이가 상한선을 초과했다면 상한 깊이로 조회한다.")
        void getAllCategories_success_withExceededRequestDepth(){
            // given
            int requestDepth = RECURSIVE_DEPTH_LIMIT + 1;
            List<CategoryTreeRow> rows = List.of(mock(CategoryTreeRow.class));
            List<CategoryResponse> responses = List.of(mock(CategoryResponse.class));

            given(categoryRepository.findAllCategoryTree(RECURSIVE_DEPTH_LIMIT))
                .willReturn(rows);
            given(categoryMapper.toHierarchy(rows)).willReturn(responses);

            // when
            List<CategoryResponse> result = categoryService.getAllCategories(requestDepth);
        
            // then
            assertThat(result).isEqualTo(responses);
            then(categoryRepository).should(times(1)).findAllCategoryTree(RECURSIVE_DEPTH_LIMIT);
        }
        
        @Test
        @DisplayName("카테고리가 존재하지 않으면 빈 리스트를 반환한다.")
        void getAllCategories_success_emptyList(){
            // given
            given(categoryRepository.findAllCategoryTree(DEFAULT_TREE_DEPTH))
                .willReturn(List.of());
            given(categoryMapper.toHierarchy(List.of())).willReturn(List.of());
        
            // when
            List<CategoryResponse> result = categoryService.getAllCategories(null);
        
            // then
            assertThat(result).isEmpty();
        }

    }

    @Nested
    @DisplayName("단일 카테고리 트리 조회 테스트")
    class GetCategoryTest {

        @Test
        @DisplayName("카테고리가 정상적으로 조회된다.")
        void getCategory_success(){
            // given
            Long categoryId = 1L;
            List<CategoryTreeRow> rows = List.of(mock(CategoryTreeRow.class));
            CategoryResponse response = mock(CategoryResponse.class);

            given(categoryRepository.findCategoryTreeById(categoryId, DEFAULT_TREE_DEPTH))
                .willReturn(rows);
            given(categoryMapper.toTree(rows)).willReturn(response);

            // when
            CategoryResponse result = categoryService.getCategory(categoryId);

            // then
            assertThat(result).isEqualTo(response);
        }
        
        @Test
        @DisplayName("존재하지 않은 ID로 요청시 예외가 발생한다.")
        void getCategory_fail_categoryNotFound(){
            // given
            Long categoryId = 999L;
            given(categoryRepository.findCategoryTreeById(categoryId, DEFAULT_TREE_DEPTH))
                .willReturn(List.of());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryService.getCategory(categoryId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);
            then(categoryMapper).should(never()).toTree(any());
        }
    }

    @Nested
    @DisplayName("카테고리 생성 테스트")
    class CreateCategoryTest {

        private CreateCategoryRequest createRequestByParentId(Long id) {
            return new CreateCategoryRequest(id, "카테고리명");
        }

        @Test
        @DisplayName("요청된 부모 카테고리 ID가 없다면 루트 카테고리를 정상적으로 생성한다.")
        void createCategory_success_rootCategory(){
            // given
            CreateCategoryRequest request = createRequestByParentId(null);
            Category saved = CategoryFixture.rootCategory();

            given(categoryRepository.existsByParentIsNullAndName(request.name()))
                .willReturn(false);
            given(categoryRepository.save(any(Category.class)))
                .willReturn(saved);

            // when
            CreateCategoryResponse result = categoryService.createCategory(request);

            // then
            assertThat(result).isNotNull();
            then(categoryRepository).should(never()).findById(any());
            then(categoryRepository).should(never()).existsByParentAndName(any(), any());
            then(categoryRepository).should(times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("요청된 부모 카테고리 ID가 있다면 자식 카테고리를 정상적으로 생성한다.")
        void createCategory_success_childCategory(){
            // given
            CreateCategoryRequest request = createRequestByParentId(1L);
            Category parent = CategoryFixture.rootCategory();
            Category saved = CategoryFixture.createChild(parent, request.name());

            given(categoryRepository.findById(1L)).willReturn(Optional.of(parent));
            given(categoryRepository.existsByParentAndName(parent, request.name()))
                .willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(saved);

            // when
            CreateCategoryResponse result = categoryService.createCategory(request);

            // then
            assertThat(result).isNotNull();
            then(categoryRepository).should(times(1)).findById(1L);
            then(categoryRepository).should(never()).existsByParentIsNullAndName(any());
            then(categoryRepository).should(times(1)).save(any(Category.class));
        }
        
        @Test
        @DisplayName("존재하지 않는 부모 카테고리 ID로 요청시 예외가 발생한다.")
        void createCategory_fail_categoryNotFound(){
            // given
            CreateCategoryRequest request = createRequestByParentId(999L);
            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryService.createCategory(request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);
            then(categoryRepository).should(never()).save(any(Category.class));
        }

        @Test
        @DisplayName("동일한 이름의 루트 카테고리가 있다면 예외가 발생한다.")
        void createCategory_fail_duplicateRootCategoryName(){
            // given
            CreateCategoryRequest request = createRequestByParentId(null);
            given(categoryRepository.existsByParentIsNullAndName(request.name()))
                .willReturn(true);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryService.createCategory(request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.DUPLICATE_CATEGORY_NAME);
            then(categoryRepository).should(never()).save(any(Category.class));
        }
        
        @Test
        @DisplayName("같은 부모 하위에 동일한 이름의 카테고리가 있다면 예외가 발생한다.")
        void createCategory_fail_duplicateChildCategoryName(){
            // given
            CreateCategoryRequest request = createRequestByParentId(1L);
            Category parent = CategoryFixture.rootCategory();

            given(categoryRepository.findById(1L)).willReturn(Optional.of(parent));
            given(categoryRepository.existsByParentAndName(parent, request.name()))
                .willReturn(true);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryService.createCategory(request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.DUPLICATE_CATEGORY_NAME);
            then(categoryRepository).should(never()).save(any(Category.class));
        }

    }


    @Nested
    @DisplayName("카테고리 삭제 테스트")
    class DeleteCategoryTest {

        @Test
        @DisplayName("카테고리가 정상적으로 삭제된다.")
        void deleteCategory_success(){
            // given
            Long categoryId = 1L;
            List<Long> descendantIds = List.of(1L, 2L, 3L);

            given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(CategoryFixture.rootCategory()));
            given(categoryRepository.findAllDescendantIds(categoryId))
                .willReturn(descendantIds);
            given(productRepository.existsByCategoryIds(descendantIds))
                .willReturn(false);
        
            // when
            categoryService.deleteCategory(categoryId);
        
            // then
            then(categoryRepository).should(times(1)).deleteAllByIdInBatch(descendantIds);
        }
        
        @Test
        @DisplayName("존재하지 않은 ID로 요청시 예외가 발생한다.")
        void deleteCategory_fail_categoryNotFound(){
            // given
            Long categoryId = 999L;
            given(categoryRepository.findById(categoryId))
                .willReturn(Optional.empty());
        
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryService.deleteCategory(categoryId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);
            then(categoryRepository).should(never()).findAllDescendantIds(any());
            then(categoryRepository).should(never()).deleteAllByIdInBatch(any());
        }
        
        @Test
        @DisplayName("카테고리에 상품이 존재한다면 예외가 발생한다.")
        void deleteCategory_fail_categoryHasProducts(){
            // given
            Long categoryId = 1L;
            List<Long> descendantIds = List.of(1L, 2L, 3L);

            given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(CategoryFixture.rootCategory()));
            given(categoryRepository.findAllDescendantIds(categoryId))
                .willReturn(descendantIds);
            given(productRepository.existsByCategoryIds(descendantIds))
                .willReturn(true);
        
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                categoryService.deleteCategory(categoryId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_HAS_PRODUCTS);
            then(categoryRepository).should(never()).deleteAllByIdInBatch(any());
        }

    }

}
