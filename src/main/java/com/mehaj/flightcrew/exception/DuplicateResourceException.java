package com.mehaj.flightcrew.exception;

/**
 * Thrown when a create/update would violate a unique business field
 * (e.g. license number, email). Translated to a 409 response by the
 * global exception handler (added in a later step) -- until then it
 * surfaces as a generic 500.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
