package com.example.demo.entity.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY("cash_on_delivery"),
    CREDIT_CARD("credit_card"),
    BANK_TRANSFER("bank_transfer"),
    WALLET("wallet");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
