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
    public ResponseEntity<Catalog> createCatalog(@RequestBody Map<String, String> request) {
        String value = request.get("value");
        if (value == null || value.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(catalogService.create(value.trim()));
    }
    
    /**
     * Update catalog (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Catalog> updateCatalog(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        String value = request.get("value");
        if (value == null || value.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(catalogService.update(id, value.trim()));
    }
    
    /**
     * Delete catalog (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCatalog(@PathVariable Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

