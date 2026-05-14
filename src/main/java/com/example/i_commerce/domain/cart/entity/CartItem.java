package com.example.i_commerce.domain.cart.entity;

import com.example.i_commerce.domain.cart.exception.CartErrorCode;
import com.example.i_commerce.global.common.entity.BaseEntity;
import com.example.i_commerce.global.exception.AppException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long productItemId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer price;

    private String displayOptionName;

    @Column(nullable = false)
    private Integer quantity;

    @Builder.Default
    private Boolean isChecked = true;


    public static CartItem of(
        Cart cart,
        Long storeId,
        Long productItemId,
        String productName,
        Integer price,
        String displayOptionName,
        Integer quantity
    ) {
        return CartItem.builder()
            .cart(cart)
            .storeId(storeId)
            .productItemId(productItemId)
            .productName(productName)
            .price(price)
            .displayOptionName(displayOptionName)
            .quantity(quantity)
            .build();
    }

    public void increaseQuantity(int amount, int stockQuantity) {
        int newQuantity = this.quantity + amount;
        if (newQuantity > stockQuantity) {
            throw new AppException(CartErrorCode.EXCEED_STOCK_QUANTITY);
        }
        this.quantity = newQuantity;
    }

}
