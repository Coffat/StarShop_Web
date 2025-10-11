package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "catalogs")
public class Catalog extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String value;

    @JsonIgnore
    @OneToMany(mappedBy = "catalog", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // Constructors
    public Catalog() {
    }

    public Catalog(String value) {
        this.value = value;
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}

