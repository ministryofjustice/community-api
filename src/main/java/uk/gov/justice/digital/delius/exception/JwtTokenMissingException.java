package uk.gov.justice.digital.delius.exception;

public class JwtTokenMissingException extends RuntimeException {
    public JwtTokenMissingException(String msg) {
        super(msg);
    }
}
