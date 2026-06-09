package com.example.i_commerce.common;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.repository.OrderProductRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ReviewIntegrationTestSupport extends IntegrationTestSupport{

    @Autowired protected MemberRepository memberRepository;
    @Autowired protected StoreRepository storeRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected OrderProductRepository orderProductRepository;
    @Autowired protected ReviewRepository reviewRepository;

    protected ReviewTestSet createReviewTestEnvironment() {
        Member buyer = memberRepository.save(createMember("buyer@test.com", "홍길동", MemberType.CUSTOMER));
        Member seller = memberRepository.save(createMember("seller@test.com", "김철수", MemberType.SELLER));

        Store store = storeRepository.save(createStore("구름스토어", seller.getId()));

        Product product = createProductWithDefaultItem("여행용 캐리어", store.getId());
        ProductItem productItem = product.getItems().get(0);

        Order order = createOrderWithProduct(buyer.getId(), product, productItem);
        OrderProduct orderProduct = order.getOrderProducts().get(0);

        return new ReviewTestSet(buyer, seller, store, product, productItem, order, orderProduct);
    }

    private Member createMember(String email, String name, MemberType role) {
        return Member.builder()
            .emailHash("mock_hash_" + email)
            .emailEncrypted(("encrypted_" + email).getBytes())
            .name(name.getBytes())
            .phoneNumber("010-1234-5678".getBytes())
            .birthday("2000-01-01".getBytes())
            .password("mock_encoded_password")
            .sex(Gender.MALE)
            .role(role)
            .isSeller(role == MemberType.SELLER)
            .build();
    }

    private Store createStore(String storeName, Long sellerId) {
        return Store.builder()
            .sellerId(sellerId)
            .storeName(storeName)
            .phoneNumber("02-1234-5678")
            .storeStatus(StoreStatus.OPEN)
            .build();
    }

    private Product createProductWithDefaultItem(String productName, Long storeId) {
        Category category = categoryRepository.save(
            Category.builder()
                .name("가방/잡화")
                .build()
        );

        Product product = Product.of(
            storeId,
            category,
            productName,
            "여행용 캐리어입니다.",
            ProductOptionType.NONE
        );

        ProductItem defaultItem = ProductItem.of(
            "SKU-" + UUID.randomUUID().toString().substring(0, 8),
            104000,
            "블랙",
            null,
            null,
            true
        );

        defaultItem.initStock(100);

        product.addItem(defaultItem);

        return productRepository.save(product);
    }

    private Order createOrderWithProduct(Long userId, Product product, ProductItem productItem) {
        Order order = Order.builder()
            .userId(userId)
            .orderStatus(OrderStatus.COMPLETED)
            .totalProductAmount(productItem.getPrice())
            .usedPointAmount(0)
            .totalPayAmount(productItem.getPrice())
            .receiverName("홍길동")
            .receiverPhone("010-1234-5678")
            .zipCode("12345")
            .address("인천광역시")
            .addressDetail("어딘가")
            .build();

        OrderProduct orderProduct = OrderProduct.builder()
            .order(order)
            .productSkuId(productItem.getId())
            .productName(product.getName())
            .orderPrice(productItem.getPrice())
            .count(1)
            .build();

        order.getOrderProducts().add(orderProduct);

        return orderRepository.save(order);
    }

    public static record ReviewTestSet(
        Member buyer,
        Member seller,
        Store store,
        Product product,
        ProductItem productItem,
        Order order,
        OrderProduct orderProduct
    ) {}
}
