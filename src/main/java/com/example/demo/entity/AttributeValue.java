package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "attributevalues",
       uniqueConstraints = @UniqueConstraint(columnNames = {"attribute_id", "product_id", "value"}))
public class AttributeValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private ProductAttribute attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String value;

    // Constructors
    public AttributeValue() {
    }

    public AttributeValue(ProductAttribute attribute, Product product, String value) {
        this.attribute = attribute;
        this.product = product;
        this.value = value;
    }

    // Getters and Setters
    public ProductAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(ProductAttribute attribute) {
        this.attribute = attribute;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
