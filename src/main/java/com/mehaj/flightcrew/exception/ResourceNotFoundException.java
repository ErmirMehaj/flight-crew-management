package com.mehaj.flightcrew.exception;

/**
 * Thrown when a requested entity does not exist. Translated to a 404
 * response by the global exception handler (added in a later step) --
 * until then it surfaces as a generic 500.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
