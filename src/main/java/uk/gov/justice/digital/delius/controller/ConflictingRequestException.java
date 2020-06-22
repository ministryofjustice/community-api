package uk.gov.justice.digital.delius.controller;

public class ConflictingRequestException extends RuntimeException {
    public ConflictingRequestException(String msg) {
        super(msg);
    }
}
