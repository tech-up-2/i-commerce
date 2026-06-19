package com.example.i_commerce.domain.product.entity.enums;


import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductItemStatus {
    ON_SALE("판매중", "판매 중인 상품입니다."),
    OFF_SALE("판매 중지", "판매 중지된 상품입니다."),
    OUT_OF_STOCK("재고 없음", "현재 재고가 없는 상품입니다."),

    ;

    private final String status;
    private final String description;

    private static final Map<ProductItemStatus, Set<ProductItemStatus>> TRANSITIONS = Map.of(
        ON_SALE,      Set.of(OFF_SALE, OUT_OF_STOCK),
        OFF_SALE,     Set.of(ON_SALE),
        OUT_OF_STOCK, Set.of(ON_SALE, OFF_SALE)
    );

    public void validateTransition(ProductItemStatus target) {
        if (this == target) return;
        if (!TRANSITIONS.get(this).contains(target)) {
            throw new AppException(ProductErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

}
