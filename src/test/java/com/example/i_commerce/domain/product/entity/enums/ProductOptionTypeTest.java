package com.example.i_commerce.domain.product.entity.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertThrows;

import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("ProductOptionType 테스트")
public class ProductOptionTypeTest {

    @Nested
    @DisplayName("validateOptionCount 테스트")
    class ValidateOptionCountTest {

        @ParameterizedTest(name = "{0}은 옵션 개수가 {1}개일 때 정상이다.")
        @MethodSource("validOptionCountSource")
        @DisplayName("옵션 개수가 일치하면 예외가 발생하지 않는다.")
        void validateOptionCount_success(ProductOptionType optionType, int count) {
            // when & then
            assertThatCode(() -> optionType.validateOptionCount(count))
                .doesNotThrowAnyException();
        }

        static Stream<Arguments> validOptionCountSource() {
            return Stream.of(
                Arguments.of(ProductOptionType.NONE, 0),
                Arguments.of(ProductOptionType.SINGLE, 1),
                Arguments.of(ProductOptionType.DOUBLE, 2)
            );
        }

        @ParameterizedTest(name = "{0}은 옵션 개수가 {1}개일 때 예외가 발생한다.")
        @MethodSource("invalidOptionCountSource")
        @DisplayName("옵션 개수가 일치하지 않으면 예외가 발생한다.")
        void validateOptionCount_fail_invalidOptionCount(ProductOptionType optionType, int count) {
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                optionType.validateOptionCount(count));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_OPTION_COUNT);
        }

        static Stream<Arguments> invalidOptionCountSource() {
            return Stream.of(
                Arguments.of(ProductOptionType.NONE, 1),
                Arguments.of(ProductOptionType.SINGLE, 0),
                Arguments.of(ProductOptionType.DOUBLE, 1)
            );
        }

    }
}
