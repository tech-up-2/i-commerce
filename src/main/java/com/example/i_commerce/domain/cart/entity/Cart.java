package com.example.i_commerce.domain.cart.entity;

import com.example.i_commerce.domain.cart.exception.CartErrorCode;
import com.example.i_commerce.global.common.entity.BaseEntity;
import com.example.i_commerce.global.exception.AppException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "carts",
    uniqueConstraints = @UniqueConstraint(columnNames = "user_id")
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String guestToken;

    private Long userId;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();

    public static Cart create(Long userId) {
        return Cart.builder()
            .userId(userId)
            .build();
    }

    public Optional<CartItem> findCartItem(Long productId) {
        return cartItems.stream()
            .filter(item -> Objects.equals(item.getProductItemId(), productId))
            .filter(item -> !item.isDeleted())
            .findFirst();
    }

    public void addItem(CartItem item) {
        this.cartItems.add(item);
    }

    public void removeItems(List<Long> cartItemIds) {
        Set<Long> requestedIds = new HashSet<>(cartItemIds);

        List<CartItem> activeItemList = cartItems.stream()
            .filter(item -> !item.isDeleted())
            .filter(item -> requestedIds.contains(item.getId()))
            .toList();

        if(activeItemList.size() != cartItemIds.size()) {
            throw new AppException(CartErrorCode.CART_ITEM_NOT_FOUND);
        }

        activeItemList.forEach(CartItem::delete);
    }
}
