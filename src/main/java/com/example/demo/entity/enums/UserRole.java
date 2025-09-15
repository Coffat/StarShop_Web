package com.example.demo.entity.enums;

public enum UserRole {
    CUSTOMER("customer"),
    STAFF("staff"),
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
