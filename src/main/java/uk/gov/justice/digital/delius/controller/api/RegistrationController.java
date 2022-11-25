package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RegistrationService;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistrationController {
    private final OffenderService offenderService;
    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(OffenderService offenderService, RegistrationService registrationService) {
        this.offenderService = offenderService;
        this.registrationService = registrationService;
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/registrations", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Registration>> getOffenderRegistrationsByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                                   final @PathVariable("offenderId") Long offenderId) {

        log.info("Call to getOffenderRegistrationsByOffenderId");
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return registrationsResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    private ResponseEntity<List<Registration>> registrationsResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new ResponseEntity<>(registrationService.registrationsFor(offenderId), HttpStatus.OK))
            .orElseGet(this::notFound);
    }

    private ResponseEntity<List<Registration>> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
