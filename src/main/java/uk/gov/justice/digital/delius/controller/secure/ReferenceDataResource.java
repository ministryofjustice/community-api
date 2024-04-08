package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationAreaWithLocalDeliveryUnits;
import uk.gov.justice.digital.delius.service.ReferenceDataService;

import java.util.List;

@Slf4j
@Tag(name = "Reference data", description = "Requires ROLE_COMMUNITY")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class ReferenceDataResource {

    private final ReferenceDataService referenceDataService;

    @Operation(description = "Return probation areas. Accepts filtering to only return active areas")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping("/probationAreas")
    public Page<KeyValue> getProbationAreaCodes(
            @Parameter(name = "active", description = "Restricts to active areas only", example = "true") final @RequestParam(name = "active", required = false) boolean restrictActive,
            @Parameter(name = "excludeEstablishments", description = "Restricts to areas that are providers, no prisons will be returned", example = "true") final @RequestParam(name = "excludeEstablishments", required = false) boolean excludeEstablishments) {

        return referenceDataService.getProbationAreasCodes(restrictActive, excludeEstablishments);
    }

    @Operation(description = "Return Probation Areas and associated Local Delivery Units. Establishments are excluded. Accepts filtering to only return active areas")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping("/probationAreas/localDeliveryUnits")
    public List<ProbationAreaWithLocalDeliveryUnits> getProbationAreasAndLocalDeliveryUnits(
            @Parameter(name = "active", description = "Restricts to active areas only", example = "true") final @RequestParam(name = "active", required = false) boolean restrictActive) {
        return referenceDataService.getProbationAreasAndLocalDeliveryUnits(restrictActive);
    }
}
