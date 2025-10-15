package com.example.demo.controller;

import com.example.demo.entity.Catalog;
import com.example.demo.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {
    
    private final CatalogService catalogService;
    
    /**
     * Get all catalogs (Public)
     */
    @GetMapping
    public ResponseEntity<List<Catalog>> getAllCatalogs() {
        return ResponseEntity.ok(catalogService.findAll());
    }
    
    /**
     * Get catalog by ID (Public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Catalog> getCatalogById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.findById(id));
    }
    
    /**
     * Create new catalog (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCatalog(@RequestBody Map<String, String> request) {
        try {
            String value = request.get("value");
            if (value == null || value.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên danh mục không được để trống");
            }
            String image = request.get("image");
            return ResponseEntity.ok(catalogService.create(value.trim(), image));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Update catalog (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCatalog(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            String value = request.get("value");
            if (value == null || value.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên danh mục không được để trống");
            }
            String image = request.get("image");
            return ResponseEntity.ok(catalogService.update(id, value.trim(), image));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Update catalog image only (Admin only)
     */
    @PatchMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Catalog> updateCatalogImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String image = request.get("image");
        return ResponseEntity.ok(catalogService.updateImage(id, image));
    }
    
    /**
     * Delete catalog (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCatalog(@PathVariable Long id) {
        try {
            catalogService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

