package com.example.i_commerce.domain.product.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.enums.StockStatus;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.AttributeFixture;
import com.example.i_commerce.domain.product.fixture.ProductItemFixture;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductItemTest {

    private ProductOptionValue optionValue1;
    private ProductOptionValue optionValue2;

    @BeforeEach
    void setUp() {
        optionValue1 = mock(ProductOptionValue.class);
        optionValue2 = mock(ProductOptionValue.class);
    }

    private ProductItem createProductItem() {
        return ProductItemFixture.defaultProductItem()
            .build();
    }

    @Nested
    @DisplayName("상품 아이템 생성 테스트")
    class CreateProductItemTest {

        @Test
        @DisplayName("옵션이 있는 아이템을 정상적으로 생성한다.")
        void of_success_withOptions() {
            // when
            ProductItem item = ProductItem.of(
                "SKU-001",
                10000,
                "블랙/M",
                optionValue1,
                optionValue2,
                false
            );

            // then
            assertThat(item.getSku()).isEqualTo("SKU-001");
            assertThat(item.getPrice()).isEqualTo(10000);
            assertThat(item.getDisplayOptionName()).isEqualTo("블랙/M");
            assertThat(item.getOptionValue1()).isEqualTo(optionValue1);
            assertThat(item.getOptionValue2()).isEqualTo(optionValue2);
            assertThat(item.getStatus()).isEqualTo(ProductItemStatus.ON_SALE);
            assertThat(item.isDefault()).isFalse();
        }

        @Test
        @DisplayName("옵션 없는 기본 아이템을 정상적으로 생성한다.")
        void of_success_withoutOptions() {
            // when
            ProductItem item = ProductItem.of(
                "SKU-001",
                10000,
                null,
                null,
                null,
                true
            );

            // then
            assertThat(item.getOptionValue1()).isNull();
            assertThat(item.getOptionValue2()).isNull();
            assertThat(item.getDisplayOptionName()).isNull();
            assertThat(item.getStatus()).isEqualTo(ProductItemStatus.ON_SALE);
            assertThat(item.isDefault()).isTrue();
        }

    }

    @Nested
    @DisplayName("속성 추가 테스트")
    class AddAttributeTest {

        @Test
        @DisplayName("속성을 정상적으로 추가한다.")
        void addAttribute_success() {
            // given
            ProductItem item = createProductItem();
            ProductAttribute attribute = ProductAttribute.of(
                AttributeFixture.defaultAttribute().build(), "속성", 1
            );

            // when
            item.addAttribute(attribute);

            // then
            assertThat(item.getAttributes()).hasSize(1);
            assertThat(item.getAttributes()).contains(attribute);
        }

        @Test
        @DisplayName("여러 개의 속성을 정상적으로 추가한다.")
        void addAttribute_success_multipleAttributes() {
            // given
            ProductItem item = createProductItem();
            ProductAttribute attribute1 = mock(ProductAttribute.class);
            ProductAttribute attribute2 = mock(ProductAttribute.class);
            ProductAttribute attribute3 = mock(ProductAttribute.class);

            // when
            item.addAttribute(attribute1);
            item.addAttribute(attribute2);
            item.addAttribute(attribute3);

            // then
            assertThat(item.getAttributes()).hasSize(3);
        }

    }

    @Nested
    @DisplayName("재고 초기화 테스트")
    class InitStockTest {

        @Test
        @DisplayName("재고를 정상적으로 초기화한다.")
        void initStock_success() {
            // given
            ProductItem item = createProductItem();
            assertThat(item.getStock()).isNull();

            // when
            item.initStock(100);

            // then
            assertThat(item.getStock()).isNotNull();
            assertThat(item.getStock().getQuantity()).isEqualTo(100);
            assertThat(item.getStock().getStatus()).isEqualTo(StockStatus.IN_STOCK);
        }

        @Test
        @DisplayName("이미 재고가 초기화된 아이템에 재초기화 요청시 예외가 발생한다.")
        void initStock_fail_stockAlreadyInitialized() {
            // given
            ProductItem item = createProductItem();
            item.initStock(100);
            assertThat(item.getStock()).isNotNull();

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                item.initStock(50));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.STOCK_ALREADY_INITIALIZED);

        }

        @Test
        @DisplayName("재고 초기화 실패 시 기존 재고가 변하지 않는다.")
        void initStock_fail_stockUnchanged() {
            // given
            ProductItem item = createProductItem();
            item.initStock(100);
            Stock originalStock = item.getStock();

            // when
            assertThatThrownBy(() -> item.initStock(50))
                .isInstanceOf(AppException.class);

            // then
            assertThat(item.getStock()).isEqualTo(originalStock);
            assertThat(item.getStock().getQuantity()).isEqualTo(100);
        }

    }

    @Nested
    @DisplayName("판매 중단 테스트")
    class DiscontinueTest {

        @Test
        @DisplayName("판매 중인 아이템을 정상적으로 판매 중단한다.")
        void discontinue_success() {
            // given
            Stock stock = mock(Stock.class);
            ProductItem item = ProductItemFixture.defaultProductItem()
                .stock(stock)
                .build();

            assertThat(item.getStatus()).isEqualTo(ProductItemStatus.ON_SALE);

            // when
            item.discontinue();

            // then
            assertThat(item.getStatus()).isEqualTo(ProductItemStatus.OFF_SALE);
            then(stock).should(times(1)).markUnavailable();
        }

        @Test
        @DisplayName("이미 판매 중단인 상태에서 판매 중단이 요청될 시 상태가 유지된다.")
        void discontinue_success_alreadyOffSale() {
            // given
            Stock stock = mock(Stock.class);
            ProductItem item = ProductItemFixture.defaultProductItem()
                .stock(stock)
                .status(ProductItemStatus.OFF_SALE)
                .build();

            item.discontinue();
            assertThat(item.getStatus()).isEqualTo(ProductItemStatus.OFF_SALE);

            // when
            item.discontinue();

            // then
            assertThat(item.getStatus()).isEqualTo(ProductItemStatus.OFF_SALE);
            then(stock).should(never()).markUnavailable();
        }

    }

}
