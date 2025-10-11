package com.example.demo.service;

import com.example.demo.entity.Catalog;
import com.example.demo.repository.CatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catalog Service for handling catalog business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CatalogService {
    
    private final CatalogRepository catalogRepository;
    
    /**
     * Find all catalogs ordered by value
     * @return List of all catalogs
     */
    public List<Catalog> findAll() {
        log.info("Fetching all catalogs");
        return catalogRepository.findAllByOrderByValueAsc();
    }
    
    /**
     * Find catalog by ID
     * @param id Catalog ID
     * @return Catalog entity
     * @throws RuntimeException if not found
     */
    public Catalog findById(Long id) {
        log.info("Fetching catalog with ID: {}", id);
        return catalogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catalog not found with id: " + id));
    }
    
    /**
     * Find catalog by value
     * @param value Catalog value
     * @return Catalog entity
     * @throws RuntimeException if not found
     */
    public Catalog findByValue(String value) {
        log.info("Fetching catalog with value: {}", value);
        return catalogRepository.findByValue(value)
            .orElseThrow(() -> new RuntimeException("Catalog not found with value: " + value));
    }
    
    /**
     * Create new catalog
     * @param value Catalog value
     * @return Created catalog
     * @throws RuntimeException if catalog already exists
     */
    @Transactional
    public Catalog create(String value) {
        log.info("Creating new catalog with value: {}", value);
        
        if (catalogRepository.existsByValue(value)) {
            log.error("Catalog already exists with value: {}", value);
            throw new RuntimeException("Catalog already exists with value: " + value);
        }
        
        Catalog catalog = new Catalog();
        catalog.setValue(value);
        
        Catalog savedCatalog = catalogRepository.save(catalog);
        log.info("Successfully created catalog with ID: {}", savedCatalog.getId());
        
        return savedCatalog;
    }
    
    /**
     * Update catalog
     * @param id Catalog ID
     * @param value New catalog value
     * @return Updated catalog
     */
    @Transactional
    public Catalog update(Long id, String value) {
        log.info("Updating catalog ID: {} with new value: {}", id, value);
        
        Catalog catalog = findById(id);
        catalog.setValue(value);
        
        Catalog updatedCatalog = catalogRepository.save(catalog);
        log.info("Successfully updated catalog ID: {}", id);
        
        return updatedCatalog;
    }
    
    /**
     * Delete catalog
     * @param id Catalog ID
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting catalog ID: {}", id);
        
        Catalog catalog = findById(id);
        catalogRepository.delete(catalog);
        
        log.info("Successfully deleted catalog ID: {}", id);
    }
}

