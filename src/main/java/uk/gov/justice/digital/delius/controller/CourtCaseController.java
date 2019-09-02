package uk.gov.justice.digital.delius.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

@RestController
@Slf4j
@Api(tags = "Offender court case")
@ConditionalOnProperty(
        name = "features.events.experimental")
public class CourtCaseController {
    private final OffenderService offenderService;
    private final ConvictionService convictionService;

    @Autowired
    public CourtCaseController(OffenderService offenderService, ConvictionService convictionService) {
        this.offenderService = offenderService;
        this.convictionService = convictionService;
    }

    @RequestMapping(value = "offenders/crn/{crn}/courtCase", method = RequestMethod.POST, consumes = "application/json")
    @JwtValidation
    public ResponseEntity<Conviction> addOffenderCourtCaseByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                 final @PathVariable("crn") String crn,
                                                                 final @RequestBody CourtCase courtCase) {
        log.info("Call to addOffenderCourtCaseByCrn");
        return addPendingConvictionReturnConvictionResponseEntityOf(offenderService.offenderIdOfCrn(crn), courtCase);
    }

    private ResponseEntity<Conviction> addPendingConvictionReturnConvictionResponseEntityOf(Optional<Long> maybeOffenderId, CourtCase courtCase) {
        return maybeOffenderId
                .map(offenderId -> new ResponseEntity<>(convictionService.addCourtCaseFor(offenderId, courtCase), HttpStatus.CREATED))
                .orElseGet(this::notFound);
    }

    private ResponseEntity<Conviction> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
