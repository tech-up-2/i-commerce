package com.example.i_commerce.domain.product.entity.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProductItemStatusTest {

    @Nested
    @DisplayName("상태 전이 검증 테스트")
    class ValidateTransition {

        @Test
        @DisplayName("허용된 상태로 전이 시 예외가 발생하지 않는다.")
        void validateTransition_success() {
            assertThatNoException()
                .isThrownBy(() -> ProductItemStatus.ON_SALE
                    .validateTransition(ProductItemStatus.OFF_SALE)
                );
        }

        @Test
        @DisplayName("동일한 상태로 전이 시 예외가 발생하지 않는다.")
        void validateTransition_success_sameStatus() {
            assertThatNoException()
                .isThrownBy(() -> ProductItemStatus.ON_SALE
                    .validateTransition(ProductItemStatus.ON_SALE)
                );
        }

        @Test
        @DisplayName("허용되지 않은 상태로 전이 시 예외가 발생한다.")
        void validateTransition_fail_invalidStatusTransition() {
            AppException exception = assertThrows(AppException.class, () ->
                ProductItemStatus.OFF_SALE
                    .validateTransition(ProductItemStatus.OUT_OF_STOCK));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_STATUS_TRANSITION);

        }

    }

}
