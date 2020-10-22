package uk.gov.justice.digital.delius.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.digital.delius.service.NoSuchUserException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class GeneralControllerAdvice {

   @ExceptionHandler(HttpClientErrorException.class)
   public ResponseEntity<String> restClientError(HttpClientErrorException e) {
       log.error("Unexpected exception", e);
       return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
   }

   @ExceptionHandler(HttpServerErrorException.class)
   public ResponseEntity<String> restServerError(HttpServerErrorException e) {
       log.error("Unexpected exception", e);
       return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
   }

   @ExceptionHandler(NoSuchUserException.class)
   public ResponseEntity<String> noSuchUser(NoSuchUserException e) {
       log.info(e.getMessage());
       return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
   }

    @ExceptionHandler(WebClientResponseException.class)
    @Order(2)
    public ResponseEntity<byte[]> handleException(final WebClientResponseException e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .status(e.getRawStatusCode())
                .body(e.getResponseBodyAsByteArray());
    }
}

