package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.i_commerce.domain.product.application.dto.ProductSearchQuery;
import com.example.i_commerce.domain.product.application.service.ProductSearchService;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.ProductSearchFixture;
import com.example.i_commerce.domain.product.presentation.request.SearchProductRequest;
import com.example.i_commerce.domain.product.presentation.response.ProductItemSearchResponse;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductSearchRepositoryCustom;
import com.example.i_commerce.domain.product.repository.enums.ProductSortType;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 단위 테스트")
public class ProductSearchServiceTest {

    @InjectMocks
    private ProductSearchService productSearchService;

    @Mock
    private ProductSearchRepositoryCustom productSearchRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Captor
    private ArgumentCaptor<ProductSearchQuery> searchQueryCaptor;

    @Nested
    @DisplayName("상품 검색 테스트")
    class SearchTest {

        @Nested
        @DisplayName("인증 상태 검증 테스트")
        class AuthValidationTest {

            @Test
            @DisplayName("비인증 사용자가 첫 페이지 요청시 정상적으로 검색된다.")
            void search_authValidation_success_guestUser() {
                // given
                SearchProductRequest request = ProductSearchFixture.defaultSearchRequest();
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                Slice<ProductItemSearchResponse> result =
                    productSearchService.search(request, pageable, false);

                // then
                assertThat(result).isNotNull();
                then(productSearchRepository).should().search(any(), eq(pageable));
            }

            @Test
            @DisplayName("비인증 사용자가 1페이지 이상 요청시 예외가 발생한다.")
            void search_authValidation_fail_guestUser_guestPageLimitExceeded() {
                // given
                SearchProductRequest request = ProductSearchFixture.defaultSearchRequest();
                Pageable pageable = PageRequest.of(1, 10);

                // when & then
                AppException exception = assertThrows(AppException.class, () ->
                    productSearchService.search(request, pageable, false));

                assertThat(exception.getErrorCode())
                    .isEqualTo(ProductErrorCode.GUEST_PAGE_LIMIT_EXCEEDED);
                then(productSearchRepository).should(never()).search(any(), any());
            }

            @Test
            @DisplayName("인증된 사용자는 페이지 제한 없이 정상적으로 검색된다.")
            void search_authValidation_success_authenticatedUser() {
                // given
                SearchProductRequest request = ProductSearchFixture.defaultSearchRequest();
                Pageable pageable = PageRequest.of(5, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                Slice<ProductItemSearchResponse> result =
                    productSearchService.search(request, pageable, true);

                // then
                assertThat(result).isNotNull();
                then(productSearchRepository).should().search(any(), eq(pageable));
            }

        }

        @Nested
        @DisplayName("키워드 처리 테스트")
        class KeywordProcessingTest {

            @Test
            @DisplayName("키워드 앞뒤의 공백이 제거되어 쿼리에 전달된다.")
            void search_keywordProcessing_success_trimmed() {
                // given
                SearchProductRequest request =
                    ProductSearchFixture.createRequestWithKeyword("  노트북  ");
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().keyword()).isEqualTo("노트북");
            }

            @Test
            @DisplayName("키워드가 null이면 쿼리에 null로 전달된다.")
            void search_keywordProcessing_success_nullKeyword() {
                // given
                SearchProductRequest request = ProductSearchFixture.defaultSearchRequest();
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().keyword()).isNull();
            }

            @Test
            @DisplayName("키워드가 공백 문자열이면 쿼리에 null로 전달된다.")
            void search_keywordProcessing_success_blankKeyword() {
                // given
                SearchProductRequest request =
                    ProductSearchFixture.createRequestWithKeyword("   ");
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().keyword()).isNull();
            }
        }

        @Nested
        @DisplayName("카테고리 처리 테스트")
        class CategoryProcessingTest {

            @Test
            @DisplayName("요청된 카테고리 ID가 없다면 빈 리스트로 쿼리에 전달된다.")
            void search_categoryProcessing_success_nullCategoryId() {
                // given
                SearchProductRequest request = ProductSearchFixture.defaultSearchRequest();
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().categoryIds()).isEmpty();
                then(categoryRepository).should(never()).findAllDescendantIds(any());
            }

            @Test
            @DisplayName("요청된 카테고리 ID가 있다면 하위 카테고리 ID가 포함된 목록이 쿼리에 전달된다.")
            void search_categoryProcessing_success_withCategoryId() {
                // given
                Long categoryId = 1L;
                List<Long> descendantIds = List.of(1L, 2L, 3L);

                SearchProductRequest request =
                    ProductSearchFixture.createRequestWithCategory(categoryId);
                Pageable pageable = PageRequest.of(0, 10);

                given(categoryRepository.findAllDescendantIds(categoryId))
                    .willReturn(descendantIds);
                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().categoryIds())
                    .containsExactlyElementsOf(descendantIds);
            }
        }

        @Nested
        @DisplayName("정렬 타입 처리 테스트")
        class SortTypeResolutionTest {

            @Test
            @DisplayName("요청된 정렬 조건과 키워드가 없으면 LATEST로 정렬된다.")
            void search_sortTypeResolution_success_nullSortType_nullKeyword() {
                // given
                SearchProductRequest request = ProductSearchFixture.defaultSearchRequest();
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().sortType())
                    .isEqualTo(ProductSortType.LATEST);
            }

            @Test
            @DisplayName("요청된 정렬 조건이 없고 키워드가 있으면 RELEVANCE로 정렬된다.")
            void search_sortTypeResolution_success_nullSortType_withKeyword() {
                // given
                SearchProductRequest request =
                    ProductSearchFixture.createRequestWithKeyword("노트북");
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().sortType())
                    .isEqualTo(ProductSortType.RELEVANCE);
            }

            @Test
            @DisplayName("요청된 정렬 조건이 RELEVANCE이고 키워드가 없으면 LATEST로 정렬된다.")
            void search_sortTypeResolution_success_relevanceSortType_nullKeyword() {
                // given
                SearchProductRequest request =
                    ProductSearchFixture.createRequestWithSortType(ProductSortType.RELEVANCE);
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().sortType())
                    .isEqualTo(ProductSortType.LATEST);
            }

            @Test
            @DisplayName("요청된 정렬 조건이 RELEVANCE이고 키워드가 있으면 RELEVANCE로 정렬된다.")
            void search_sortTypeResolution_success_relevanceSortType_withKeyword() {
                // given
                SearchProductRequest request = ProductSearchFixture.defaultRequest()
                    .keyword("노트북")
                    .sortType(ProductSortType.RELEVANCE)
                    .build();

                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().sortType())
                    .isEqualTo(ProductSortType.RELEVANCE);
            }

            @Test
            @DisplayName("요청된 정렬 조건이 RELEVANCE가 아니면 요청 조건대로 정렬된다.")
            void search_sortTypeResolution_success_nonRelevanceSortType() {
                // given
                SearchProductRequest request =
                    ProductSearchFixture.createRequestWithSortType(ProductSortType.PRICE_ASC);
                Pageable pageable = PageRequest.of(0, 10);

                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));
                assertThat(searchQueryCaptor.getValue().sortType())
                    .isEqualTo(ProductSortType.PRICE_ASC);
            }
        }

        @Nested
        @DisplayName("쿼리 조합 검증 테스트")
        class QueryCompositionTest {

            @Test
            @DisplayName("모든 필드가 올바르게 쿼리에 조합되어 전달된다.")
            void search_queryComposition_success() {
                // given
                Long categoryId = 1L;
                List<Long> descendantIds = List.of(1L, 2L, 3L);
                List<Long> attributeIds = List.of(10L, 20L);

                SearchProductRequest request = ProductSearchFixture.createFullRequest(
                    "노트북", categoryId, 10000, 50000,
                    attributeIds, ProductSortType.PRICE_ASC
                );
                Pageable pageable = PageRequest.of(0, 10);

                given(categoryRepository.findAllDescendantIds(categoryId))
                    .willReturn(descendantIds);
                given(productSearchRepository
                    .search(any(ProductSearchQuery.class), eq(pageable)))
                    .willReturn(new SliceImpl<>(List.of()));

                // when
                productSearchService.search(request, pageable, true);

                // then
                then(productSearchRepository)
                    .should().search(searchQueryCaptor.capture(), eq(pageable));

                ProductSearchQuery query = searchQueryCaptor.getValue();
                assertThat(query.keyword()).isEqualTo("노트북");
                assertThat(query.categoryIds()).containsExactlyElementsOf(descendantIds);
                assertThat(query.minPrice()).isEqualTo(10000);
                assertThat(query.maxPrice()).isEqualTo(50000);
                assertThat(query.attributeIds()).containsExactlyElementsOf(attributeIds);
                assertThat(query.sortType()).isEqualTo(ProductSortType.PRICE_ASC);
                assertThat(query.isAuthenticated()).isTrue();
            }
        }
    }

}
