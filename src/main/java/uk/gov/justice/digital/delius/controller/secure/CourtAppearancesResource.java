package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasicWrapper;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceMinimalWrapper;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@Slf4j
@Tag(name = "Court appearances", description = "Requires ROLE_COMMUNITY")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class CourtAppearancesResource {
    private final CourtAppearanceService courtAppearanceService;
    private final OffenderService offenderService;

    public CourtAppearancesResource(CourtAppearanceService courtAppearanceService, OffenderService offenderService) {
        this.courtAppearanceService = courtAppearanceService;
        this.offenderService = offenderService;
    }

    @Operation(description = "Returns all court appearances on and after the given date.")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(value = "/courtAppearances")
    public CourtAppearanceMinimalWrapper getCourtAppearances(@Parameter(name = "fromDate", description = "Return court appearances from the given date. Defaults to today if not provided.", example = "2019-03-02")
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final @RequestParam("fromDate") Optional<LocalDate> fromDate) {
        final var courtAppearances = courtAppearanceService.courtAppearances(fromDate.orElse(LocalDate.now()));
        return new CourtAppearanceMinimalWrapper(courtAppearances);
    }

    @Operation(description = "Returns all court appearances associated with the CRN for the conviction ID.")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "The offender CRN is not found"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(value = "/offenders/crn/{crn}/convictions/{convictionId}/courtAppearances")
    public CourtAppearanceBasicWrapper getOffenderCourtAppearancesByCrn(final @PathVariable("crn") String crn,
                                                                        final @PathVariable("convictionId") Long convictionId) {

        return offenderService.offenderIdOfCrn(crn)
            .map((offenderId) -> courtAppearanceService.courtAppearancesFor(offenderId, convictionId))
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)))
            .orElseThrow(() -> new NotFoundException(String.format("Conviction with ID %s for Offender with crn %s not found", convictionId, crn)));
    }
}

