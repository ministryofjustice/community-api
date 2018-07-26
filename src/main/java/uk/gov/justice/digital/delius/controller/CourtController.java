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
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@Api(description = "Offender court resources", tags = "Offender Court")
public class CourtController {

    private final OffenderService offenderService;
    private final CourtAppearanceService courtAppearanceService;

    @Autowired
    public CourtController(OffenderService offenderService, CourtAppearanceService courtAppearanceService) {
        this.offenderService = offenderService;
        this.courtAppearanceService = courtAppearanceService;
    }

    @RequestMapping(value = "offender/crn/{crn}/courtAppearances", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<CourtAppearance>> getOffenderCourtAppearances(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @PathVariable("crn") String crn) {

        log.info("Call to getOffenderCourtAppearances");
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
