package com.example.i_commerce.domain.product.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.common.ProductIntegrationTestSupport;
import com.example.i_commerce.domain.product.application.service.ProductSearchService;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import com.example.i_commerce.domain.product.fixture.CategoryFixture;
import com.example.i_commerce.domain.product.fixture.ProductFixture;
import com.example.i_commerce.domain.product.fixture.ProductItemFixture;
import com.example.i_commerce.domain.product.presentation.request.SearchProductRequest;
import com.example.i_commerce.domain.product.presentation.response.ProductItemSearchResponse;
import com.example.i_commerce.domain.product.repository.enums.ProductSortType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@DisplayName("상품 검색 통합 테스트")
public class ProductSearchIntegrationTest extends ProductIntegrationTestSupport {

    @Autowired
    private ProductSearchService productSearchService;

    private Category electronicsCategory;
    private Category phoneCategory;
    private Category clothingCategory;

    @BeforeEach
    void setUp() {
        electronicsCategory = CategoryFixture.createRootWithName("전자기기");
        phoneCategory = CategoryFixture.createChild( electronicsCategory, "스마트폰");
        clothingCategory = CategoryFixture.createRootWithName("의류");
        categoryRepository.saveAll(List.of(electronicsCategory, phoneCategory, clothingCategory));

        Product phoneProduct = ProductFixture.createProductBy(
            phoneCategory, "아이폰 15", ProductStatus.ON_SALE
        );
        Product samsungProduct = ProductFixture.createProductBy(
            phoneCategory, "갤럭시 S24", ProductStatus.ON_SALE
        );
        Product laptopProduct = ProductFixture.createProductBy(
            electronicsCategory, "맥북 프로", ProductStatus.DISCONTINUED
        );
        Product tShirtProduct = ProductFixture.createProductBy(
            clothingCategory, "반팔 티셔츠", ProductStatus.ON_SALE
        );

        ProductItem phoneItem = ProductItemFixture.createProductItemBy(
            phoneProduct, 1_200_000, ProductItemStatus.ON_SALE
        );
        ProductItem samsungItem = ProductItemFixture.createProductItemBy(
            samsungProduct, 1_100_000, ProductItemStatus.ON_SALE
        );
        ProductItem laptopItem = ProductItemFixture.createProductItemBy(
            laptopProduct, 2_000_000, ProductItemStatus.ON_SALE
        );
        ProductItem tShirtItem = ProductItemFixture.createProductItemBy(
            tShirtProduct, 29_000, ProductItemStatus.ON_SALE
        );

        ProductItemFixture.createProductAttributeBy(phoneItem, 1L, "색상: 블루");
        ProductItemFixture.createProductAttributeBy(phoneItem, 2L, "제조사: 애플");
        ProductItemFixture.createProductAttributeBy(samsungItem, 1L, "색상: 블랙");
        ProductItemFixture.createProductAttributeBy(samsungItem, 2L, "제조사: 삼성");
        ProductItemFixture.createProductAttributeBy(tShirtItem, 1L, "색상: 화이트");

        productRepository.saveAll(List.of(phoneProduct, samsungProduct, laptopProduct,
            tShirtProduct));
    }

    @Nested
    @DisplayName("키워드 검색 테스트")
    class KeywordSearchTest {

        @Test
        @DisplayName("상품명으로 검색하면 일치하는 상품만 반환된다.")
        void searchByProductName() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .keyword("아이폰")
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().productName()).isEqualTo("아이폰 15");
        }

        @Test
        @DisplayName("카테고리명으로 검색하면 해당 카테고리 상품이 반환된다.")
        void searchByCategoryName() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .keyword("스마트폰")
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(ProductItemSearchResponse::productName)
                .containsExactlyInAnyOrder("아이폰 15", "갤럭시 S24");
        }

        @Test
        @DisplayName("속성명으로 검색하면 해당 속성을 가진 상품이 반환된다.")
        void searchByAttributeName() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .keyword("애플")
                .sortType(ProductSortType.LATEST)
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().productName()).isEqualTo("아이폰 15");
        }

        @Test
        @DisplayName("키워드가 null이면 전체 판매중 상품이 반환된다")
        void searchWithNullKeyword() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .keyword(null)
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("카테고리 필터 테스트")
    class CategoryFilterTest {

        @Test
        @DisplayName("상위 카테고리로 필터링하면 하위 카테고리 상품도 포함된다.")
        void filterByParentCategoryIncludesChildren() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .categoryId(electronicsCategory.getId())
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(ProductItemSearchResponse::productName)
                .containsExactlyInAnyOrder("아이폰 15", "갤럭시 S24");
        }

        @Test
        @DisplayName("하위 카테고리로 필터링하면 해당 카테고리 상품만 반환된다.")
        void filterByLeafCategory() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .categoryId(clothingCategory.getId())
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().productName()).isEqualTo("반팔 티셔츠");
        }
    }

    @Nested
    @DisplayName("속성 필터 테스트")
    class AttributeFilterTest {

        @Test
        @DisplayName("단일 속성 ID로 필터링하면 해당 속성을 가진 상품만 반환된다")
        void filterBySingleAttribute() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .attributeIds(List.of(2L))
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(ProductItemSearchResponse::productName)
                .containsExactlyInAnyOrder("아이폰 15", "갤럭시 S24");
        }

        @Test
        @DisplayName("여러 속성 ID는 AND 조건으로 필터링된다")
        void filterByMultipleAttributesWithAnd() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .attributeIds(List.of(1L, 2L))
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(ProductItemSearchResponse::productName)
                .containsExactlyInAnyOrder("아이폰 15", "갤럭시 S24");
        }
    }

    @Nested
    @DisplayName("가격 필터 테스트")
    class PriceFilterTest {

        @Test
        @DisplayName("최소 가격 이상의 상품만 반환된다.")
        void filterByMinPrice() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .minPrice(1_000_000)
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(ProductItemSearchResponse::price)
                .allMatch(price -> price >= 1_000_000);
        }

        @Test
        @DisplayName("최대 가격 이하의 상품만 반환된다.")
        void filterByMaxPrice() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .maxPrice(50_000)
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().price()).isEqualTo(29_000);
        }

        @Test
        @DisplayName("최소, 최대 가격 범위 내 상품만 반환된다.")
        void filterByPriceRange() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .minPrice(1_000_000)
                .maxPrice(1_150_000)
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().productName()).isEqualTo("갤럭시 S24");
        }

    }

    @Nested
    @DisplayName("복합 조건 검색 테스트")
    class CombinedFilterTest {

        @Test
        @DisplayName("키워드 + 카테고리 + 가격 복합 조건으로 검색된다.")
        void searchWithMultipleConditions() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .keyword("갤럭시")
                .categoryId(phoneCategory.getId())
                .minPrice(1_000_000)
                .maxPrice(1_200_000)
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().productName()).isEqualTo("갤럭시 S24");
        }

        @Test
        @DisplayName("조건에 맞는 상품이 없으면 빈 결과가 반환된다.")
        void returnsEmptyWhenNoMatch() {
            // given
            SearchProductRequest request = SearchProductRequest.builder()
                .keyword("존재하지않는상품명")
                .build();

            // when
            Slice<ProductItemSearchResponse> result = productSearchService.search(
                request, PageRequest.of(0, 10), true
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

}
