package com.mehaj.flightcrew.exception;

/**
 * Thrown when an operation is attempted against a flight that isn't in
 * the required status for that operation (e.g. modifying or assigning
 * to a flight that's already COMPLETED or CANCELLED). Translated to a
 * 409 response by the global exception handler (added in a later step).
 */
public class InvalidFlightStateException extends RuntimeException {

    public InvalidFlightStateException(String message) {
        super(message);
    }
}
