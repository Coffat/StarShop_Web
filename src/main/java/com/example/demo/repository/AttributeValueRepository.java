package com.example.demo.repository;

import com.example.demo.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    
    List<AttributeValue> findByProductId(Long productId);
    
    List<AttributeValue> findByAttributeId(Long attributeId);
    
    @Query("SELECT DISTINCT av.value FROM AttributeValue av WHERE av.attribute.id = :attributeId")
    List<String> findDistinctValuesByAttributeId(@Param("attributeId") Long attributeId);
    
    void deleteByProductId(Long productId);
}
