package com.aghayev.ecommerce.exception;

public class UnauthorizedException extends RuntimeException {

    private final String fieldName;

    public UnauthorizedException(String message) {
        this(message, null);
    }

    public UnauthorizedException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
