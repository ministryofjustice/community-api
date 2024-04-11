package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RiskService;

import java.util.Optional;

@Tag(name = "Risks and Registrations")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class RiskResource {
    private final RiskService riskService;
    private final OffenderService offenderService;

    @Operation(description = "Return the MAPPA details for an offender using CRN. requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ROLE_COMMUNITY"),
            @ApiResponse(responseCode = "404", description = "Offender not found"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(value = "offenders/crn/{crn}/risk/mappa")
    public MappaDetails getOffenderMappaDetailsByCrn(final @PathVariable("crn") String crn) {
        return mappaDetailsFor(offenderService.offenderIdOfCrn(crn));
    }

    private MappaDetails mappaDetailsFor(final Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(riskService::getMappaDetails)
            .orElseThrow(() -> new NotFoundException("Offender not found"));
    }
}
