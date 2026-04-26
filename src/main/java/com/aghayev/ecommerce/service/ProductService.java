package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.dto.ProductRequestDto;
import com.aghayev.ecommerce.dto.ProductResponseDto;
import com.aghayev.ecommerce.entity.Product;
import com.aghayev.ecommerce.exception.ResourceNotFoundException;
import com.aghayev.ecommerce.repository.ProductRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponseDto> getProducts(Pageable pageable, String category) {
        Page<Product> products = category == null || category.isBlank()
                ? productRepository.findAll(pageable)
                : productRepository.findByCategory(category, pageable);

        return products.map(this::toResponseDto);
    }

    public ProductResponseDto getProductById(UUID id) {
        return toResponseDto(findProductById(id));
    }

    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        Product product = Product.builder()
                .name(requestDto.name())
                .description(requestDto.description())
                .price(requestDto.price())
                .stockQuantity(requestDto.stockQuantity())
                .category(requestDto.category())
                .build();

        return toResponseDto(productRepository.save(product));
    }

    public ProductResponseDto updateProduct(UUID id, ProductRequestDto requestDto) {
        Product product = findProductById(id);

        product.setName(requestDto.name());
        product.setDescription(requestDto.description());
        product.setPrice(requestDto.price());
        product.setStockQuantity(requestDto.stockQuantity());
        product.setCategory(requestDto.category());

        return toResponseDto(productRepository.save(product));
    }

    public void deleteProduct(UUID id) {
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponseDto toResponseDto(Product product) {
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
