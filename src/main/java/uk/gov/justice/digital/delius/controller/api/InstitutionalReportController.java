package uk.gov.justice.digital.delius.controller.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.InstitutionalReport;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.InstitutionalReportService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;


@RestController
@Slf4j
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class InstitutionalReportController {

    private final OffenderService offenderService;
    private final InstitutionalReportService institutionalReportService;

    @Autowired
    public InstitutionalReportController(OffenderService offenderService, InstitutionalReportService institutionalReportService) {
        this.offenderService = offenderService;
        this.institutionalReportService = institutionalReportService;
    }

    @RequestMapping(value = "offenders/crn/{crn}/institutionalReports/{institutionalReportId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<InstitutionalReport> getOffenderInstitutionalReportByCrnAndInstitutionalReportId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                              final @PathVariable("crn") String crn,
                                                                                              final @PathVariable Long institutionalReportId) {

        return institutionalReportResponseEntityOf(offenderService.offenderIdOfCrn(crn), institutionalReportId);
    }

    private ResponseEntity<InstitutionalReport> institutionalReportResponseEntityOf(Optional<Long> maybeOffenderId,  Long institutionalReportId) {
        Optional<InstitutionalReport> maybeInstitutionalReport = maybeOffenderId.flatMap(offenderId -> institutionalReportService.institutionalReportFor(offenderId, institutionalReportId));
        return maybeInstitutionalReport.map(
                institutionalReport -> new ResponseEntity<>(institutionalReport, OK)).orElse(institutionalReportNotFound());

    }

    private ResponseEntity<InstitutionalReport> institutionalReportNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
