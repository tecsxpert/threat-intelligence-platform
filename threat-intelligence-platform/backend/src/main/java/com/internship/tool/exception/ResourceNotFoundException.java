package com.internship.tool.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Long fieldId) {
        super(String.format("%s not found with id : '%s'", resourceName, fieldId));
    }
}
