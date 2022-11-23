package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
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
@Api(description = "Offender institutional report resources", tags = "Offender institutional Reports")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class InstitutionalReportController {

    private final OffenderService offenderService;
    private final InstitutionalReportService institutionalReportService;

    @Autowired
    public InstitutionalReportController(OffenderService offenderService, InstitutionalReportService institutionalReportService) {
        this.offenderService = offenderService;
        this.institutionalReportService = institutionalReportService;
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/institutionalReports", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<InstitutionalReport>> getOffenderInstitutionalReportsByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                     final @PathVariable("offenderId") Long offenderId) {

        log.info("Call to getOffenderInstitutionalReportsByOffenderId");
        return institutionalReportsResponseEntityOf(Optional.of(offenderId));
    }

    @RequestMapping(value = "offenders/crn/{crn}/institutionalReports", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<InstitutionalReport>> getOffenderInstitutionalReportsByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @PathVariable("crn") String crn) {

        log.info("Call to getOffenderInstitutionalReportsByCrn");
        return institutionalReportsResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/institutionalReports/{institutionalReportId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<InstitutionalReport> getOffenderInstitutionalReportByOffenderIdAndInstitutionalReportId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                 final @PathVariable("offenderId") Long offenderId,
                                                                                 final @PathVariable Long institutionalReportId) {

        log.info("Call to getOffenderInstitutionalReportByOffenderIdAndInstitutionalReportId");
        return institutionalReportResponseEntityOf(Optional.of(offenderId), institutionalReportId);
    }

    @RequestMapping(value = "offenders/crn/{crn}/institutionalReports/{institutionalReportId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<InstitutionalReport> getOffenderInstitutionalReportByCrnAndInstitutionalReportId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                              final @PathVariable("crn") String crn,
                                                                                              final @PathVariable Long institutionalReportId) {

        log.info("Call to getOffenderInstitutionalReportByCrnAndInstitutionalReportId");
        return institutionalReportResponseEntityOf(offenderService.offenderIdOfCrn(crn), institutionalReportId);
    }

    private ResponseEntity<List<InstitutionalReport>> institutionalReportsResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new ResponseEntity<>(institutionalReportService.institutionalReportsFor(offenderId), OK))
            .orElseGet(this::institutionalReportsNotFound);
    }

    private ResponseEntity<InstitutionalReport> institutionalReportResponseEntityOf(Optional<Long> maybeOffenderId,  Long institutionalReportId) {
        Optional<InstitutionalReport> maybeInstitutionalReport = maybeOffenderId.flatMap(offenderId -> institutionalReportService.institutionalReportFor(offenderId, institutionalReportId));
        return maybeInstitutionalReport.map(
                institutionalReport -> new ResponseEntity<>(institutionalReport, OK)).orElse(institutionalReportNotFound());

    }


    private ResponseEntity<List<InstitutionalReport>> institutionalReportsNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<InstitutionalReport> institutionalReportNotFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
