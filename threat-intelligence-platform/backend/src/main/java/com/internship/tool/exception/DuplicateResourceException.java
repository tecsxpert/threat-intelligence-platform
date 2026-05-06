package com.internship.tool.exception;

// Thrown when user tries to create something that already exists
// Example: creating a threat with a title that already exists
// Returns HTTP 409 Conflict
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String entityName, String field, String value) {
        super(entityName + " already exists with " + field + ": " + value);
    }
}