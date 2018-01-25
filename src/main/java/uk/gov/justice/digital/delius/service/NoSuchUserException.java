package uk.gov.justice.digital.delius.service;

public class NoSuchUserException extends RuntimeException {
    public NoSuchUserException(String msg) {
        super(msg);
    }
}