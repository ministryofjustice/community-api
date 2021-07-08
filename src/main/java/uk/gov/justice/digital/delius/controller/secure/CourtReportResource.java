package uk.gov.justice.digital.delius.controller.secure;

import java.util.Optional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.UserAccessService;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Api(tags = {"Offender Court Reports"}, authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@AllArgsConstructor
public class CourtReportResource {

    private final OffenderService offenderService;
    private final CourtReportService courtReportService;
    private final UserAccessService userAccessService;

    @ApiOperation(value = "Return the CourtReport for an offender and report ID", tags = {"Court Report"})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
            @ApiResponse(code = 404, message = "The offender or report is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @RequestMapping(value = "offenders/crn/{crn}/courtReports/{courtReportId}", method = RequestMethod.GET)
    public ResponseEntity<CourtReportMinimal> getOffenderCourtReportsByCrn(final @PathVariable("crn") String crn,
                                                                        final @PathVariable Long courtReportId,
                                                                        final Authentication authentication) {
        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());

        log.info("Call to getOffenderCourtReportByCrnAndCourtReportId");
        return courtReportResponseEntityOf(offenderService.offenderIdOfCrn(crn), courtReportId);
    }

    private ResponseEntity<CourtReportMinimal> courtReportResponseEntityOf(Optional<Long> maybeOffenderId,  Long courtReportId) {
        final var maybeCourtReport = maybeOffenderId.flatMap(offenderId -> courtReportService.courtReportMinimalFor(offenderId, courtReportId));
        return maybeCourtReport.map(courtReport -> new ResponseEntity<>(courtReport, OK))
                                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

}
