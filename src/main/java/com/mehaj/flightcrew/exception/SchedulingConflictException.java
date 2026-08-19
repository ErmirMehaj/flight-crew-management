package com.mehaj.flightcrew.exception;

/**
 * Thrown when an assignment would violate a scheduling constraint:
 * a double-booked aircraft, a pilot who is unavailable or already flying
 * an overlapping flight, or a crew member who would exceed max working
 * hours. Translated to a 409 response by the global exception handler
 * (added in a later step).
 */
public class SchedulingConflictException extends RuntimeException {

    public SchedulingConflictException(String message) {
        super(message);
    }
}
