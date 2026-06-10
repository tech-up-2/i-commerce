package com.example.i_commerce.domain.product.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


public class CategoryTest {

    @Nested
    @DisplayName("자식 카테고리 생성 테스트")
    class CreateChild {

        @Test
        @DisplayName("자식 카테고리를 정상적으로 생성한다.")
        void createChild_success() {
            // given
            Category root = Category.createRoot("전자제품");

            // when
            Category child = Category.createChild(root, "노트북", 3);

            // then
            assertThat(child.getName()).isEqualTo("노트북");
            assertThat(child.getParent()).isEqualTo(root);
            assertThat(child.getDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("자식의 자식 카테고리를 정상적으로 생성한다.")
        void createChild_success_grandChild() {
            // given
            Category root = Category.createRoot("전자제품");
            Category child = Category.createChild(root, "노트북", 3);

            // when
            Category grandChild = Category.createChild(child, "맥", 3);

            // then
            assertThat(grandChild.getDepth()).isEqualTo(2);
            assertThat(grandChild.getParent()).isEqualTo(child);
        }

        @Test
        @DisplayName("최대 depth를 초과하면 예외가 발생한다.")
        void createChild_fail_categoryDepth_exceeded() {
            // given
            Category root = Category.createRoot("전자제품");
            Category child = Category.createChild(root, "노트북", 3);
            Category grandChild = Category.createChild(child, "맥", 3);
            int maxDepth = 2;

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                Category.createChild(grandChild, "초고사양", maxDepth));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_DEPTH_EXCEEDED);

        }

    }

}
