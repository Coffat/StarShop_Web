package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliveryunits")
public class DeliveryUnit extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "estimated_time", length = 50)
    private String estimatedTime;

    @OneToMany(mappedBy = "deliveryUnit", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    // Constructors
    public DeliveryUnit() {
    }

    public DeliveryUnit(String name, BigDecimal fee, String estimatedTime) {
        this.name = name;
        this.fee = fee;
        this.estimatedTime = estimatedTime;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
