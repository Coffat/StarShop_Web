package com.example.demo.entity.enums;

public enum OrderStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    SHIPPED("shipped"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
