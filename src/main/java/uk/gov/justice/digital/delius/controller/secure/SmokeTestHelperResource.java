package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderDetails;
import uk.gov.justice.digital.delius.service.SmokeTestHelperService;

@Tag(name = "Smoke test")
@RestController
@Slf4j
@RequestMapping(value = "secure/smoketest", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_SMOKE_TEST')")
@ConditionalOnProperty(name = "smoke.test.aware", havingValue = "true")
public class SmokeTestHelperResource {
    private final SmokeTestHelperService smokeTestHelperService;

    public SmokeTestHelperResource(SmokeTestHelperService smokeTestHelperService) {
        this.smokeTestHelperService = smokeTestHelperService;
    }

    @RequestMapping(value = "offenders/crn/{crn}/custody/reset", method = RequestMethod.POST, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_SMOKE_TEST"),
            @ApiResponse(responseCode = "404", description = "Either the requested offender was not found or no active custodial sentences were found")
    })
    @Operation(description = "Resets custody data to the state before a Delius offender record has been matched to a NOMIS record. Only used for smoke tests, not production ready")
    public void resetCustodySmokeTestData(@PathVariable String crn) {
        smokeTestHelperService.resetCustodySmokeTestData(crn);
    }

    @RequestMapping(value = "offenders/crn/{crn}/details", method = RequestMethod.POST, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_SMOKE_TEST"),
            @ApiResponse(responseCode = "404", description = "The requested offender was not found")
    })
    @Operation(description = "Updates the specified offenders details. Only used for smoke tests, not production ready")
    public void updateOffenderDetails(@PathVariable String crn, final @RequestBody @Valid UpdateOffenderDetails offenderDetails) {
        smokeTestHelperService.updateOffenderDetails(crn, offenderDetails);
    }

}
