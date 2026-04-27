package com.aghayev.ecommerce.mapper;

import com.aghayev.ecommerce.dto.request.ProductRequestDto;
import com.aghayev.ecommerce.dto.response.ProductResponseDto;
import com.aghayev.ecommerce.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto requestDto) {
        return Product.builder()
                .name(requestDto.name())
                .description(requestDto.description())
                .price(requestDto.price())
                .stockQuantity(requestDto.stockQuantity())
                .category(requestDto.category())
                .build();
    }

    public void updateEntity(Product product, ProductRequestDto requestDto) {
        product.setName(requestDto.name());
        product.setDescription(requestDto.description());
        product.setPrice(requestDto.price());
        product.setStockQuantity(requestDto.stockQuantity());
        product.setCategory(requestDto.category());
    }

    public ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
