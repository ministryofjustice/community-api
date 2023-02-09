package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uk.gov.justice.digital.delius.data.api.CourtReportMinimal;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CourtReportService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.UserAccessService;

import java.util.List;

@RestController
@Slf4j
@Tag(name = "Offender Court Reports", description = "Requires ROLE_COMMUNITY")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@AllArgsConstructor
public class CourtReportResource {

    private final ConvictionService convictionService;
    private final OffenderService offenderService;
    private final CourtReportService courtReportService;
    private final UserAccessService userAccessService;

    @Operation(description = "Return the CourtReports for an offender and a conviction ID", tags = {"Court appearances"})
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden, the offender may have exclusions or restrictions in place preventing some users from viewing. Adopting the client scopes SCOPE_IGNORE_DELIUS_INCLUSIONS_ALWAYS and SCOPE_IGNORE_DELIUS_EXCLUSIONS_ALWAYS can bypass these restrictions."),
            @ApiResponse(responseCode = "404", description = "The offender or conviction ID is not found"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
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

}
