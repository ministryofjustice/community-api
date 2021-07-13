package uk.gov.justice.digital.delius.controller.secure;

import java.util.List;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.UserAccessService;

@RestController
@Slf4j
@Api(tags = {"Offender Court Reports"}, authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@AllArgsConstructor
public class CourtReportResource {

    private final ConvictionService convictionService;
    private final OffenderService offenderService;
    private final CourtReportService courtReportService;
    private final UserAccessService userAccessService;

    @ApiOperation(value = "Return the CourtReport for an offender and report ID", tags = {"Court appearances"})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
            @ApiResponse(code = 404, message = "The offender or report is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @RequestMapping(value = "offenders/crn/{crn}/courtReports/{courtReportId}", method = RequestMethod.GET)
    public CourtReportMinimal getOffenderCourtReportByCrnAndCourtReportId(final @PathVariable("crn") String crn,
                                                                        final @PathVariable Long courtReportId,
                                                                        final Authentication authentication) {
        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());

        log.info("Call to getOffenderCourtReportByCrnAndCourtReportId");
        final var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
        return courtReportResponseEntityOf(offenderId, courtReportId);
    }

    @ApiOperation(value = "Return the CourtReports for an offender and a conviction ID", tags = {"Court appearances"})
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
            @ApiResponse(code = 404, message = "The offender or conviction ID is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @RequestMapping(value = "offenders/crn/{crn}/convictions/{convictionId}/courtReports", method = RequestMethod.GET)
    public List<CourtReportMinimal> getOffenderCourtReportsByCrnAndConvictionId(final @PathVariable("crn") String crn,
                                                                                final @PathVariable("convictionId") Long convictionId,
                                                                                final Authentication authentication) {

        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());

        final var offenderId = offenderService.offenderIdOfCrn(crn).orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));

        return convictionService.eventFor(offenderId, convictionId)
            .map(ev -> courtReportsResponseEntityOf(offenderId, convictionId))
            .orElseThrow(() -> new NotFoundException(String.format("Conviction ID of %s for offender with crn %s not found", convictionId, crn)));
    }

    private List<CourtReportMinimal> courtReportsResponseEntityOf(Long offenderId, Long convictionId) {

        return courtReportService.courtReportsMinimalFor(offenderId, convictionId);
    }


    private CourtReportMinimal courtReportResponseEntityOf(Long offenderId,  Long courtReportId) {
        return courtReportService.courtReportMinimalFor(offenderId, courtReportId)
                .orElseThrow(() -> new NotFoundException(String.format("Court report with ID %s not found", courtReportId)));
    }

}
