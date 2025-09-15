package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productattributes")
public class ProductAttribute extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttributeValue> attributeValues = new ArrayList<>();

    // Constructors
    public ProductAttribute() {
    }

    public ProductAttribute(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AttributeValue> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<AttributeValue> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
