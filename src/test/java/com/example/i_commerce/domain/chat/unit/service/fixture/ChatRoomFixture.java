package com.example.i_commerce.domain.chat.unit.service.fixture;

import com.example.i_commerce.domain.chat.entity.ChatRoom;
import com.example.i_commerce.domain.chat.util.ChatRoomNameGenerator;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;

public class ChatRoomFixture {
    public static ChatRoom createChatGroupRoom(Long id, String name, Long productId) {
        return ChatRoom.builder()
            .id(id)
            .isGroupChat(true)
            .name(name)
            .productId(productId)
            .build();
    }
    public static ChatRoom createChatPrivateRoom(Long id, String name) {
        return ChatRoom.builder()
            .id(id)
            .isGroupChat(false)
            .name(name)
            .build();
    }
    public static Category createCategory(Long id) {
        return Category.builder()
            .id(id)
            .name("테스트 카테고라")
            .depth(1)
            .build();
    }
    public static Product createProduct(Long id, Long storeId, Category category) {
        return Product.builder()
            .id(id)
            .name("테스트 상품")
            .description("테스트 상품의 설명")
            .storeId(storeId)
            .category(category)
            .optionType(ProductOptionType.SINGLE)
            .status(ProductStatus.ON_SALE)
            .build();
    }
}
