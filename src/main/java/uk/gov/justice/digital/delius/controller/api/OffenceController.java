package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.OffenceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@Api(description = "Offender offence resources", tags = "Offender offences")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class OffenceController {
    private final OffenderService offenderService;
    private final OffenceService offenceService;

    @Autowired
    public OffenceController(OffenderService offenderService, OffenceService offenceService) {
        this.offenderService = offenderService;
        this.offenceService = offenceService;
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/offences", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Offence>> getOffenderOffencesByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("offenderId") Long offenderId) {

        log.info("Call to getOffenderOffencesByOffenderId");
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return offencesResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @RequestMapping(value = "offenders/crn/{crn}/offences", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Offence>> getOffenderOffencesByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("crn") String crn) {

        log.info("Call to getOffenderOffencesByCrn");
        return offencesResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    private ResponseEntity<List<Offence>> offencesResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new ResponseEntity<>(offenceService.offencesFor(offenderId), HttpStatus.OK))
            .orElseGet(this::notFound);
    }

    private ResponseEntity<List<Offence>> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
