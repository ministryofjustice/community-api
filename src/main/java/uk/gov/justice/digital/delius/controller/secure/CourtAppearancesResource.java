package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasicWrapper;
import uk.gov.justice.digital.delius.service.CourtAppearanceService;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@Slf4j
@Api(tags = {"Court appearance resource"}, authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class CourtAppearancesResource {
    private final CourtAppearanceService courtAppearanceService;

    public CourtAppearancesResource(CourtAppearanceService courtAppearanceService) {
        this.courtAppearanceService = courtAppearanceService;
    }

    @ApiOperation(value = "Returns future court appearances, including any for the current date")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/courtAppearances")
    public CourtAppearanceBasicWrapper getCourtAppearances(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final @RequestParam("fromDate") Optional<LocalDate> fromDate) {
        final var courtAppearanceBasics = courtAppearanceService.courtAppearances(fromDate.orElse(LocalDate.now()));
        return new CourtAppearanceBasicWrapper(courtAppearanceBasics);
    }
}

