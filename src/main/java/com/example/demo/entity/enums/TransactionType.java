package com.example.demo.entity.enums;

public enum TransactionType {
    PAYMENT("payment"),
    REFUND("refund");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
