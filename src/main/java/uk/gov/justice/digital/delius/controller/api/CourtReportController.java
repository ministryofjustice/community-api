package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Api(description = "Offender court report resources", tags = "Offender Court Reports")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CourtReportController {

    private final OffenderService offenderService;
    private final CourtReportService courtReportService;

    @Autowired
    public CourtReportController(OffenderService offenderService, CourtReportService courtReportService) {
        this.offenderService = offenderService;
        this.courtReportService = courtReportService;
    }

    @RequestMapping(value = "offenders/crn/{crn}/courtReports", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<CourtReport>> getOffenderCourtReportsByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @PathVariable("crn") String crn) {

        log.info("Call to getOffenderCourtReportsByCrn");
        return courtReportsResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    @RequestMapping(value = "offenders/crn/{crn}/courtReports/{courtReportId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<CourtReport> getOffenderCourtReportByCrnAndCourtReportId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                              final @PathVariable("crn") String crn,
                                                                                              final @PathVariable Long courtReportId) {

        log.info("Call to getOffenderCourtReportByCrnAndCourtReportId");
        return courtReportResponseEntityOf(offenderService.offenderIdOfCrn(crn), courtReportId);
    }

    private ResponseEntity<List<CourtReport>> courtReportsResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new ResponseEntity<>(courtReportService.courtReportsFor(offenderId), OK))
            .orElseGet(this::courtReportsNotFound);
    }

    private ResponseEntity<CourtReport> courtReportResponseEntityOf(Optional<Long> maybeOffenderId,  Long courtReportId) {
        Optional<CourtReport> maybeCourtReport = maybeOffenderId.flatMap(offenderId -> courtReportService.courtReportFor(offenderId, courtReportId));
        return maybeCourtReport.map(
                courtReport -> new ResponseEntity<>(courtReport, OK)).orElse(courtReportNotFound());

    }


    private ResponseEntity<List<CourtReport>> courtReportsNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<CourtReport> courtReportNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
