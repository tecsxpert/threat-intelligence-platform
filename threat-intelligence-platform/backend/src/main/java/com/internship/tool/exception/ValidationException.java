package com.internship.tool.exception;

// Thrown when user sends invalid data
// Example: title is empty, severity is not a valid value
// The controller advice will catch this and return HTTP 400
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}