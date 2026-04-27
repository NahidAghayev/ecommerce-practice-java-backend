package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.config.LogExecutionTime;
import com.aghayev.ecommerce.dto.ProductRequestDto;
import com.aghayev.ecommerce.dto.ProductResponseDto;
import com.aghayev.ecommerce.entity.Product;
import com.aghayev.ecommerce.exception.ResourceNotFoundException;
import com.aghayev.ecommerce.repository.ProductRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @LogExecutionTime
    public Page<ProductResponseDto> getProducts(Pageable pageable, String category) {
        log.debug(
                "action=getProducts page={} size={} sort={} category={}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort(),
                category
        );
        Page<Product> products = category == null || category.isBlank()
                ? productRepository.findAll(pageable)
                : productRepository.findByCategory(category, pageable);

        return products.map(this::toResponseDto);
    }

    @LogExecutionTime
    public ProductResponseDto getProductById(UUID id) {
        log.debug("action=getProductById productId={}", id);
        return toResponseDto(findProductById(id));
    }

    @LogExecutionTime
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        log.info(
                "action=createProduct name={} category={} price={} stockQuantity={}",
                requestDto.name(),
                requestDto.category(),
                requestDto.price(),
                requestDto.stockQuantity()
        );
        Product product = Product.builder()
                .name(requestDto.name())
                .description(requestDto.description())
                .price(requestDto.price())
                .stockQuantity(requestDto.stockQuantity())
                .category(requestDto.category())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("action=createProduct status=SUCCESS productId={}", savedProduct.getId());
        return toResponseDto(savedProduct);
    }

    @LogExecutionTime
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto requestDto) {
        log.info(
                "action=updateProduct productId={} name={} category={} price={} stockQuantity={}",
                id,
                requestDto.name(),
                requestDto.category(),
                requestDto.price(),
                requestDto.stockQuantity()
        );
        Product product = findProductById(id);

        product.setName(requestDto.name());
        product.setDescription(requestDto.description());
        product.setPrice(requestDto.price());
        product.setStockQuantity(requestDto.stockQuantity());
        product.setCategory(requestDto.category());

        Product updatedProduct = productRepository.save(product);
        log.info("action=updateProduct status=SUCCESS productId={}", updatedProduct.getId());
        return toResponseDto(updatedProduct);
    }

    @LogExecutionTime
    public void deleteProduct(UUID id) {
        log.info("action=deleteProduct productId={}", id);
        Product product = findProductById(id);
        productRepository.delete(product);
        log.info("action=deleteProduct status=SUCCESS productId={}", id);
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
