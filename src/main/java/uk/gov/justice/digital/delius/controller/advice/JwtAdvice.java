package uk.gov.justice.digital.delius.controller.advice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;

@ControllerAdvice
public class JwtAdvice {
    @ExceptionHandler(JwtTokenMissingException.class)
    public ResponseEntity<String> missingJwt(JwtTokenMissingException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> badJwt(MalformedJwtException e) {
        return new ResponseEntity<>("Bad Token.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> expiredJwt(ExpiredJwtException e) {
        return new ResponseEntity<>("Expired Token.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> notMine(SignatureException e) {
        return new ResponseEntity<>("Invalid signature.", HttpStatus.FORBIDDEN);
    }
}
