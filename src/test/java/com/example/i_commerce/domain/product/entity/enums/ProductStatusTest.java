package com.example.i_commerce.domain.product.entity.enums;

import static com.example.i_commerce.domain.product.entity.enums.ProductStatus.DISCONTINUED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductStatusTest {

    @Nested
    @DisplayName("하위 전파 여부 테스트")
    class RequiresItemCascadeTest {
        
        @Test
        @DisplayName("DISCONTINUED 상태면 전파가 요구된다.")
        void requiresItemCascade_statusDiscontinued(){
            assertThat(ProductStatus.DISCONTINUED
                .requiresItemCascade()).isTrue();
        }

        @Test
        @DisplayName("DISCONTINUED 상태가 아니면 전파가 요구되지 않는다.")
        void requiresItemCascade_statusNotDiscontinued(){
            assertThat(ProductStatus.ON_SALE
                .requiresItemCascade()).isFalse();
        }

    }

    @Nested
    @DisplayName("상태 전이 가능 여부 테스트")
    class CanTransitionToTest {

        @Test
        @DisplayName("PENDING에서 ON_SALE로 전이할 수 있다.")
        void canTransitionTo_pendingToOnSale() {
            assertThat(ProductStatus.PENDING
                .canTransitionTo(ProductStatus.ON_SALE)).isTrue();
        }

        @Test
        @DisplayName("PENDING에서 DISCONTINUED로 전이할 수 있다.")
        void canTransitionTo_pendingToDiscontinued() {
            assertThat(ProductStatus.PENDING
                .canTransitionTo(DISCONTINUED)).isTrue();
        }

        @Test
        @DisplayName("ON_SALE에서 PENDING으로 전이할 수 있다.")
        void canTransitionTo_onSaleToPending() {
            assertThat(ProductStatus.ON_SALE
                .canTransitionTo(ProductStatus.PENDING)).isTrue();
        }

        @Test
        @DisplayName("ON_SALE에서 DISCONTINUED로 전이할 수 있다.")
        void canTransitionTo_onSaleToDiscontinued() {
            assertThat(ProductStatus.ON_SALE
                .canTransitionTo(DISCONTINUED)).isTrue();
        }

        @Test
        @DisplayName("DISCONTINUED에서 PENDING으로 전이할 수 있다.")
        void canTransitionTo_discontinuedToPending() {
            assertThat(DISCONTINUED.
                canTransitionTo(ProductStatus.PENDING)).isTrue();
        }

        @Test
        @DisplayName("DISCONTINUED에서 ON_SALE로 전이할 수 없다.")
        void canTransitionTo_discontinuedToOnSale_returnsFalse() {
            assertThat(DISCONTINUED
                .canTransitionTo(ProductStatus.ON_SALE)).isFalse();
        }

        @Test
        @DisplayName("PENDING에서 PENDING으로 전이할 수 없다.")
        void canTransitionTo_pendingToPending_returnsFalse() {
            assertThat(ProductStatus
                .PENDING.canTransitionTo(ProductStatus.PENDING)).isFalse();
        }

        @Test
        @DisplayName("ON_SALE에서 ON_SALE로 전이할 수 없다.")
        void canTransitionTo_onSaleToOnSale_returnsFalse() {
            assertThat(ProductStatus.ON_SALE
                .canTransitionTo(ProductStatus.ON_SALE)).isFalse();
        }

    }

    @Nested
    @DisplayName("상태 전이 검증 테스트")
    class ValidateTransitionTest {

        @Test
        @DisplayName("허용된 상태로 전이 시 예외가 발생하지 않는다.")
        void validateTransition_success() {
            // when & then
            assertThatNoException().isThrownBy(() ->
                ProductStatus.ON_SALE.validateTransition(ProductStatus.PENDING));
        }

        @Test
        @DisplayName("동일한 상태로 전이 시 예외가 발생하지 않는다.")
        void validateTransition_success_sameStatus() {
            assertThatNoException().isThrownBy(() ->
                ProductStatus.ON_SALE.validateTransition(ProductStatus.ON_SALE));
        }

        @Test
        @DisplayName("허용되지 않은 상태로 전이 시 예외가 발생한다.")
        void validateTransition_fali_invalidStatusTransition() {
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                DISCONTINUED.validateTransition(ProductStatus.ON_SALE));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }

    }


}
