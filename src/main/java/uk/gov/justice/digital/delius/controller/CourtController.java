package uk.gov.justice.digital.delius.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@Api(description = "Offender court appearance resources", tags = "Offender Court Appearances")
public class CourtController {

    private final OffenderService offenderService;
    private final CourtAppearanceService courtAppearanceService;

    @Autowired
    public CourtController(OffenderService offenderService, CourtAppearanceService courtAppearanceService) {
        this.offenderService = offenderService;
        this.courtAppearanceService = courtAppearanceService;
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/courtAppearances", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<CourtAppearance>> getOffenderCourtAppearancesByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @PathVariable("offenderId") Long offenderId) {

        log.info("Call to getOffenderCourtAppearancesByOffenderId");
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return courtAppearancesResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/courtAppearances", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<CourtAppearance>> getOffenderCourtAppearancesByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @PathVariable("nomsNumber") String nomsNumber) {

        log.info("Call to getOffenderCourtAppearancesByNomsNumber");
        return courtAppearancesResponseEntityOf(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @RequestMapping(value = "offenders/crn/{crn}/courtAppearances", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<CourtAppearance>> getOffenderCourtAppearancesByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @PathVariable("crn") String crn) {

        log.info("Call to getOffenderCourtAppearancesByCrn");
        return courtAppearancesResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    private ResponseEntity<List<CourtAppearance>> courtAppearancesResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new ResponseEntity<>(courtAppearanceService.courtAppearancesFor(offenderId), HttpStatus.OK))
            .orElseGet(this::notFound);
    }

    private ResponseEntity<List<CourtAppearance>> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
