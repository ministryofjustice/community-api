package uk.gov.justice.digital.delius.controller;

public class UnauthorisedException extends RuntimeException {
    public UnauthorisedException(String msg) {
        super(msg);
    }
}