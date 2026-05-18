package com.aghayev.ecommerce.service;

import com.aghayev.ecommerce.config.LogExecutionTime;
import com.aghayev.ecommerce.dto.PageResponse;
import com.aghayev.ecommerce.dto.request.ProductRequestDto;
import com.aghayev.ecommerce.dto.response.ProductResponseDto;
import com.aghayev.ecommerce.entity.Product;
import com.aghayev.ecommerce.exception.BadRequestException;
import com.aghayev.ecommerce.exception.ResourceNotFoundException;
import com.aghayev.ecommerce.mapper.ProductMapper;
import com.aghayev.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.UUID;

import com.aghayev.ecommerce.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @LogExecutionTime
    public PageResponse<ProductResponseDto> getProducts(
            Pageable pageable,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice

    ) {
        log.debug(
                "action=getProducts page={} size={} sort={} category={} minPrice={} maxPrice={}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort(),
                category,
                minPrice,
                maxPrice
        );

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price", "minPrice");
        }

        Specification<Product> spec = Specification.where(null);

        if (category != null && !category.isBlank()) {
            spec = spec.and(ProductSpecification.hasCategory(category));
        }
        if (minPrice != null) {
            spec = spec.and(ProductSpecification.priceGreaterThanOrEqualTo(minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and(ProductSpecification.priceLessThanOrEqualTo(maxPrice));
        }

        Page<Product> products = productRepository.findAll(spec, pageable);

        Page<ProductResponseDto> mappedProducts = products.map(productMapper::toResponseDto);
        return PageResponse.from(mappedProducts);
    }

    @LogExecutionTime
    public ProductResponseDto getProductById(UUID id) {
        log.debug("action=getProductById productId={}", id);
        return productMapper.toResponseDto(findProductById(id));
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
        Product product = productMapper.toEntity(requestDto);

        Product savedProduct = productRepository.save(product);
        log.info("action=createProduct status=SUCCESS productId={}", savedProduct.getId());
        return productMapper.toResponseDto(savedProduct);
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

        productMapper.updateEntity(product, requestDto);

        Product updatedProduct = productRepository.save(product);
        log.info("action=updateProduct status=SUCCESS productId={}", updatedProduct.getId());
        return productMapper.toResponseDto(updatedProduct);
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
}
