package uk.gov.justice.digital.delius.controller.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Tag(description = "Offender court report resources", name = "Offender Court Reports")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CourtReportController {

    private final OffenderService offenderService;
    private final CourtReportService courtReportService;

    @Autowired
    public CourtReportController(OffenderService offenderService, CourtReportService courtReportService) {
        this.offenderService = offenderService;
        this.courtReportService = courtReportService;
    }

    @RequestMapping(value = "offenders/crn/{crn}/courtReports/{courtReportId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<CourtReport> getOffenderCourtReportByCrnAndCourtReportId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                              final @PathVariable("crn") String crn,
                                                                                              final @PathVariable Long courtReportId) {

        return courtReportResponseEntityOf(offenderService.offenderIdOfCrn(crn), courtReportId);
    }

    private ResponseEntity<CourtReport> courtReportResponseEntityOf(Optional<Long> maybeOffenderId,  Long courtReportId) {
        Optional<CourtReport> maybeCourtReport = maybeOffenderId.flatMap(offenderId -> courtReportService.courtReportFor(offenderId, courtReportId));
        return maybeCourtReport.map(
                courtReport -> new ResponseEntity<>(courtReport, OK)).orElse(courtReportNotFound());

    }

    private ResponseEntity<CourtReport> courtReportNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
