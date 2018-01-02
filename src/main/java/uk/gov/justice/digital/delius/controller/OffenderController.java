package uk.gov.justice.digital.delius.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.views.Views;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@Log
public class OffenderController {

    private final OffenderService offenderService;

    @Autowired
    public OffenderController(OffenderService offenderService) {
        this.offenderService = offenderService;
    }

    @RequestMapping(value = "/offenders/{offenderId}", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.OffenderOnly.class)
    public ResponseEntity<OffenderDetail> getOffender(final @RequestHeader HttpHeaders httpHeaders,
                                                      final @PathVariable("offenderId") Long offenderId) {
        return offenderResponseOf(offenderId);
    }

    @RequestMapping(value = "/offenders/{offenderId}/detail", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.FullFat.class)
    public ResponseEntity<OffenderDetail> getFullFatOffender(final @RequestHeader HttpHeaders httpHeaders,
                                                      final @PathVariable("offenderId") Long offenderId) {
        return offenderResponseOf(offenderId);
    }

    private ResponseEntity<OffenderDetail> offenderResponseOf(@PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> offender = offenderService.getOffender(offenderId);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)
        ).orElse(notFound());
    }

    private ResponseEntity<OffenderDetail> notFound() {
        return new ResponseEntity<>(NOT_FOUND);
    }

    @ExceptionHandler(JwtTokenMissingException.class)
    public ResponseEntity<String> missingJwt(JwtTokenMissingException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> badJwt(MalformedJwtException e) {
        return new ResponseEntity<>("Bad Token.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> expiredJwt(ExpiredJwtException e) {
        return new ResponseEntity<>("Expired Token.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> notMine(SignatureException e) {
        return new ResponseEntity<>("Invalid signature.", HttpStatus.UNAUTHORIZED);
    }

}
