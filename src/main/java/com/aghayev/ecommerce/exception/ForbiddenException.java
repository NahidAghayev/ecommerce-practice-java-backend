package com.aghayev.ecommerce.exception;

public class ForbiddenException extends RuntimeException {

    private final String fieldName;

    public ForbiddenException(String message) {
        this(message, null);
    }

    public ForbiddenException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
