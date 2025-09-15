package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews")
    List<Product> findAllWithReviews();
    
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p LEFT JOIN p.orderItems oi GROUP BY p ORDER BY COUNT(oi) DESC")
    List<Product> findBestSellingProducts(Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.attributeValues av WHERE av.attribute.id = :attributeId AND av.value = :value")
    List<Product> findByAttributeValue(@Param("attributeId") Long attributeId, @Param("value") String value);
}
