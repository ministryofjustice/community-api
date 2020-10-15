package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
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
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasicWrapper;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@Slf4j
@Api(tags = {"Offender Court Appearances (Secure)"}, authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class CourtAppearancesResource {
    private final CourtAppearanceService courtAppearanceService;
    private final OffenderService offenderService;

    public CourtAppearancesResource(CourtAppearanceService courtAppearanceService, OffenderService offenderService) {
        this.courtAppearanceService = courtAppearanceService;
        this.offenderService = offenderService;
    }

    @ApiOperation(value = "Returns all court appearances on and after the given date.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/courtAppearances")
    public CourtAppearanceBasicWrapper getCourtAppearances(@ApiParam(name = "fromDate", value = "Return court appearances from the given date. Defaults to today if not provided.", example = "2019-03-02", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final @RequestParam("fromDate") Optional<LocalDate> fromDate) {
        final var courtAppearanceBasics = courtAppearanceService.courtAppearances(fromDate.orElse(LocalDate.now()));
        return new CourtAppearanceBasicWrapper(courtAppearanceBasics);
    }

    @ApiOperation(value = "Returns all court appearances associated with the CRN for the conviction ID.")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender CRN is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
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

