package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/catalogs")
@RequiredArgsConstructor
public class ProductCatalogController {
    
    private final ProductService productService;
    
    /**
     * Get products by catalog ID
     * @param catalogId Catalog ID
     * @param page Page number (default: 0)
     * @param size Page size (default: 12)
     * @return Page of products
     */
    @GetMapping("/{catalogId}")
    public ResponseEntity<Page<Product>> getProductsByCatalog(
            @PathVariable Long catalogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.findByCatalog(catalogId, pageable));
    }
}

