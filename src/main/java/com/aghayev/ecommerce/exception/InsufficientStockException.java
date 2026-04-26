package com.aghayev.ecommerce.exception;

public class InsufficientStockException extends RuntimeException {

    private final String fieldName;

    public InsufficientStockException(String message) {
        this(message, null);
    }

    public InsufficientStockException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
