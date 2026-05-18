package com.example.i_commerce.domain.product.entity;


import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ProductStatus {
    ON_SALE("판매중", "판매되고 있는 상품입니다."),
    PENDING("판매 대기", "판매 대기중인 상품입니다."),
    DISCONTINUED("판매 중단", "판매가 중단된 상품입니다.");

    private final String status;
    private final String description;

    private static final Map<ProductStatus, Set<ProductStatus>> TRANSITIONS = Map.of(
        PENDING,      Set.of(ON_SALE, DISCONTINUED),
        ON_SALE,      Set.of(PENDING, DISCONTINUED),
        DISCONTINUED, Set.of(PENDING)
    );

    public boolean requiresItemCascade() {
        return this == DISCONTINUED;
    }

    public boolean canTransitionTo(ProductStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    public void validateTransition(ProductStatus target) {
        if (this == target) return;
        if (!canTransitionTo(target)) {
            throw new AppException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
