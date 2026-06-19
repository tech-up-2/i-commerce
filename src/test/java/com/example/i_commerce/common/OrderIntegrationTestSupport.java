package com.example.i_commerce.common;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.repository.OrderProductRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentHistoryRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.Stock;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import com.example.i_commerce.domain.product.entity.enums.StockStatus;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.StockHistoryRepository;
import com.example.i_commerce.domain.product.repository.StockRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public abstract class OrderIntegrationTestSupport extends IntegrationTestSupport {
    @Autowired protected DeliveryRepository deliveryRepository;
    @Autowired protected PaymentRepository paymentRepository;
    @Autowired protected OrderRepository orderRepository;
    @Autowired protected OrderProductRepository orderProductRepository;
    @Autowired protected DeliveryAddressRepository deliveryAddressRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected ProductRepository productRepository;
    @Autowired protected ProductItemRepository productItemRepository;
    @Autowired protected StockRepository stockRepository;
    @Autowired protected MockMvc mockMvc;
    @Autowired protected PaymentHistoryRepository paymentHistoryRepository;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        deliveryRepository.deleteAllInBatch();
        paymentHistoryRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
    }

    protected CommerceTestSet saveDefaultCommerceTestSet(Long memberId) {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);


        DeliveryAddress address = DeliveryAddress.builder()
                .memberId(memberId)
                .label("집")
                .recipientName(dataEncryptor.encrypt("홍길동"))
                .recipientPhone(dataEncryptor.encrypt("01012345678"))
                .zipCode(dataEncryptor.encrypt("12345"))
                .roadAddress(dataEncryptor.encrypt("서울특별시 강남구 테헤란로"))
                .detailAddress(dataEncryptor.encrypt("101호"))
                .build();

        DeliveryAddress savedAddress = deliveryAddressRepository.save(address);

        Category category = categoryRepository.save(Category.builder()
                .name("전자기기")
                .depth(0)
                .build()
        );

        Product product = productRepository.save(Product.builder()
                .name("최고급 맥북 프로")
                .storeId(1L)
                .optionType(ProductOptionType.NONE)
                .status(ProductStatus.ON_SALE)
                .category(category)
                .build());

        ProductItem item1 = ProductItem.builder()
                .product(product)
                .price(1500000)
                .status(ProductItemStatus.ON_SALE)
                .sku("asdf" + uniqueId)
                .mainImageUrl("")
                .displayOptionName("")
                .build();

        ProductItem item2 = ProductItem.builder()
                .product(product)
                .price(50000)
                .status(ProductItemStatus.ON_SALE)
                .sku("qwer" + uniqueId)
                .mainImageUrl("")
                .displayOptionName("")
                .build();

        productItemRepository.saveAll(List.of(item1, item2));

        Stock stock1 = stockRepository.save(Stock.builder()
                .productItem(item1)
                .quantity(5)
                .status(StockStatus.IN_STOCK)
                .build()
        );

        Stock stock2 = stockRepository.save(Stock.builder()
                .productItem(item2)
                .quantity(5)
                .status(StockStatus.IN_STOCK)
                .build()
        );

        return new CommerceTestSet(
                savedAddress,
                product,
                List.of(item1, item2),
                List.of(stock1, stock2)
        );

    }

    protected record CommerceTestSet(
            DeliveryAddress address,
            Product product,
            List<ProductItem> items,
            List<Stock> stocks
    ) {}
}
