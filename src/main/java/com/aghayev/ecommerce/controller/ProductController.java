package com.aghayev.ecommerce.controller;

import com.aghayev.ecommerce.dto.ApiResponse;
import com.aghayev.ecommerce.dto.PageResponse;
import com.aghayev.ecommerce.dto.request.ProductRequestDto;
import com.aghayev.ecommerce.dto.response.ProductResponseDto;
import com.aghayev.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getProducts(
            Pageable pageable,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProducts(pageable, category),
                "Products retrieved successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProductById(id),
                "Product retrieved successfully"
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto createdProduct = productService.createProduct(requestDto);
        return ResponseEntity
                .created(URI.create("/api/products/" + createdProduct.id()))
                .body(ApiResponse.success(createdProduct, "Product created successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequestDto requestDto
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.updateProduct(id, requestDto),
                "Product updated successfully"
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}
