package com.internship.tool.exception;

// Thrown when a user tries to do something they are not allowed to do
// Example: a regular user trying to delete a record only admins can delete
// Returns HTTP 403 Forbidden
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}