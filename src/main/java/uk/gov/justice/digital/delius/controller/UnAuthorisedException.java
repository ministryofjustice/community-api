package uk.gov.justice.digital.delius.controller;

public class UnAuthorisedException extends RuntimeException {
    public UnAuthorisedException(String msg) {
        super(msg);
    }
}