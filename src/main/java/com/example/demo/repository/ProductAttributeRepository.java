package com.example.demo.repository;

import com.example.demo.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    
    Optional<ProductAttribute> findByName(String name);
    
    boolean existsByName(String name);
}
