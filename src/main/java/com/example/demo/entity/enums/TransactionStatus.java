package com.example.demo.entity.enums;

public enum TransactionStatus {
    SUCCESS("success"),
    FAILED("failed");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
