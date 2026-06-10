package com.example.i_commerce.domain.product.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.ProductFixture;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = mock(Category.class);
    }

    private Product createProduct() {
        return ProductFixture.defaultProduct()
            .category(category)
            .build();
    }

    private ProductItem createMockItem(String sku) {
        ProductItem item = mock(ProductItem.class);
        given(item.getSku()).willReturn(sku);
        return item;
    }

    @Nested
    @DisplayName("상품 생성 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("상품을 정상적으로 생성한다.")
        void of_success() {
            // when
            Product product = Product.of(
                1L,
                category,
                "테스트 상품",
                "상품 설명",
                ProductOptionType.SINGLE
            );

            // then
            assertThat(product.getStoreId()).isEqualTo(1L);
            assertThat(product.getCategory()).isEqualTo(category);
            assertThat(product.getName()).isEqualTo("테스트 상품");
            assertThat(product.getDescription()).isEqualTo("상품 설명");
            assertThat(product.getOptionType()).isEqualTo(ProductOptionType.SINGLE);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
            assertThat(product.getItems()).isEmpty();
            assertThat(product.getOptions()).isEmpty();
            assertThat(product.getImages()).isEmpty();
        }

    }

    @Nested
    @DisplayName("상품 아이템 추가 테스트")
    class AddItemTest {

        @Test
        @DisplayName("상품 아이템 단건을 정상적으로 추가한다.")
        void addItem_success() {
            // given
            Product product = createProduct();
            ProductItem item = createMockItem("SKU-001");

            // when
            product.addItem(item);

            // then
            assertThat(product.getItems()).hasSize(1);
            assertThat(product.getItems()).contains(item);
            then(item).should(times(1)).setProduct(product);
        }

        @Test
        @DisplayName("SKU가 다른 여러개의 아이템을 정상적으로 추가한다.")
        void addItem_success_multipleItems() {
            // given
            Product product = createProduct();
            ProductItem item1 = createMockItem("SKU-001");
            ProductItem item2 = createMockItem("SKU-002");
            ProductItem item3 = createMockItem("SKU-003");

            // when
            product.addItem(item1);
            product.addItem(item2);
            product.addItem(item3);

            // then
            assertThat(product.getItems()).hasSize(3);
        }

        @Test
        @DisplayName("추가 요청된 아이템에 중복된 SKU가 존재하면 예외가 발생한다.")
        void addItem_fail_duplicatedSku() {
            // given
            Product product = createProduct();
            ProductItem item1 = createMockItem("SKU-001");
            ProductItem item2 = createMockItem("SKU-001");

            product.addItem(item1);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                product.addItem(item2));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.DUPLICATED_SKU);

            then(item2).should(never()).setProduct(product);
        }

        @Test
        @DisplayName("예외 발생 시 기존 아이템 목록이 변하지 않는다.")
        void addItem_success_itemsUnchanged() {
            // given
            Product product = createProduct();
            ProductItem item1 = createMockItem("SKU-001");
            ProductItem item2 = createMockItem("SKU-001");

            product.addItem(item1);

            // when
            assertThatThrownBy(() -> product.addItem(item2))
                .isInstanceOf(AppException.class);

            // then
            assertThat(product.getItems()).hasSize(1);
            assertThat(product.getItems()).containsExactly(item1);
        }

    }

    @Nested
    @DisplayName("옵션 값 추가 테스트")
    class AddOptionValueTest {

        @Test
        @DisplayName("옵션 값을 정상적으로 추가한다.")
        void addOptionValue_success() {
            // given
            Product product = createProduct();
            ProductOptionValue optionValue = ProductOptionValue.of(
                1, "옵션명", "옵션값", 1
            );

            // when
            product.addOptionValue(optionValue);

            // then
            assertThat(product.getOptions()).hasSize(1);
            assertThat(product.getOptions()).contains(optionValue);
        }

        @Test
        @DisplayName("여러 개의 옵션 값을 정상적으로 추가한다.")
        void addOptionValue_success_multipleOptions() {
            // given
            Product product = createProduct();
            ProductOptionValue option1 = mock(ProductOptionValue.class);
            ProductOptionValue option2 = mock(ProductOptionValue.class);

            // when
            product.addOptionValue(option1);
            product.addOptionValue(option2);

            // then
            assertThat(product.getOptions()).hasSize(2);
            then(option1).should(times(1)).setProduct(product);
            then(option2).should(times(1)).setProduct(product);
        }

    }

    @Nested
    @DisplayName("아이템 또는 기본 아이템 조회 테스트")
    class FindItemOrDefaultTest {

        @Test
        @DisplayName("요청된 아이템 ID가 없다면 기본 아이템을 정상적으로 반환한다.")
        void findItemOrDefault_success_itemIdNull_returnsDefaultItem() {
            // given
            Product product = createProduct();
            ProductItem defaultItem = createMockItem("SKU-001");
            ProductItem nonDefaultItem = createMockItem("SKU-002");

            given(defaultItem.isDefault()).willReturn(true);
            given(nonDefaultItem.isDefault()).willReturn(false);

            product.addItem(nonDefaultItem);
            product.addItem(defaultItem);

            // when
            ProductItem result = product.findItemOrDefault(null);

            // then
            assertThat(result).isEqualTo(defaultItem);
        }

        @Test
        @DisplayName("요청된 아이템 ID가 없고 기본 아이템이 없으면 예외가 발생한다.")
        void findItemOrDefault_fail_itemIdNull_defaultItemNotFound() {
            // given
            Product product = createProduct();
            ProductItem item = createMockItem("SKU-001");
            given(item.isDefault()).willReturn(false);
            product.addItem(item);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                product.findItemOrDefault(null));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.DEFAULT_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("요청받은 아이템 ID로 특정 아이템을 정상적으로 조회한다.")
        void findItemOrDefault_success() {
            // given
            Product product = createProduct();
            ProductItem item1 = createMockItem("SKU-001");
            ProductItem item2 = createMockItem("SKU-002");

            given(item1.getId()).willReturn(1L);
            given(item2.getId()).willReturn(2L);

            product.addItem(item1);
            product.addItem(item2);

            // when
            ProductItem result = product.findItemOrDefault(2L);

            // then
            assertThat(result).isEqualTo(item2);
        }

        @Test
        @DisplayName("존재하지 않는 아이템 ID가 요청되면 예외가 발생한다.")
        void findItemOrDefault_fail_productItemNotFound() {
            // given
            Product product = createProduct();
            ProductItem item = createMockItem("SKU-001");
            given(item.getId()).willReturn(1L);
            product.addItem(item);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                product.findItemOrDefault(999L));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_ITEM_NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("기본 정보 수정 테스트")
    class UpdateBasicInfoTest {

        @Test
        @DisplayName("상품 기본 정보를 정상적으로 수정한다.")
        void updateBasicInfo_success() {
            // given
            Product product = createProduct();

            // when
            product.updateBasicInfo("수정 상품명", "수정 설명");

            // then
            assertThat(product.getName()).isEqualTo("수정 상품명");
            assertThat(product.getDescription()).isEqualTo("수정 설명");
        }

    }

    @Nested
    @DisplayName("상태 변경 테스트")
    class ChangeStatusTest {

        @Test
        @DisplayName("ON_SALE에서 PENDING으로 상태를 성공적으로 변경한다.")
        void changeStatus_success_onSaleToPending() {
            // given
            Product product = createProduct();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);

            // when
            product.changeStatus(ProductStatus.PENDING);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING);
        }

        @Test
        @DisplayName("동일한 상태로 변경이 요청되면 상태가 유지된다.")
        void changeStatus_success_sameStatus() {
            // given
            Product product = createProduct();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);

            // when
            product.changeStatus(ProductStatus.ON_SALE);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        }

        @Test
        @DisplayName("DISCONTINUED 상태로 변경 시 모든 아이템의 상태도 변경된다.")
        void changeStatus_success_changeAllItems() {
            // given
            Product product = createProduct();
            ProductItem item1 = createMockItem("SKU-001");
            ProductItem item2 = createMockItem("SKU-002");
            product.addItem(item1);
            product.addItem(item2);

            // when
            product.changeStatus(ProductStatus.DISCONTINUED);

            // then
            then(item1).should(times(1)).discontinue();
            then(item2).should(times(1)).discontinue();
        }

    }

}