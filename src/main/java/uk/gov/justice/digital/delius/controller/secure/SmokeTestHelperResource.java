package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

import jakarta.validation.Valid;

@Api(tags = "Smoke test")
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
            @ApiResponse(code = 403, message = "Requires role ROLE_SMOKE_TEST"),
            @ApiResponse(code = 404, message = "Either the requested offender was not found or no active custodial sentences were found")
    })
    @ApiOperation(value = "Resets custody data to the state before a Delius offender record has been matched to a NOMIS record", notes = "Only used for smoke tests, not production ready")
    public void resetCustodySmokeTestData(@PathVariable String crn) {
        smokeTestHelperService.resetCustodySmokeTestData(crn);
    }

    @RequestMapping(value = "offenders/crn/{crn}/details", method = RequestMethod.POST, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_SMOKE_TEST"),
            @ApiResponse(code = 404, message = "The requested offender was not found")
    })
    @ApiOperation(value = "Updates the specified offenders details", notes = "Only used for smoke tests, not production ready")
    public void updateOffenderDetails(@PathVariable String crn, final @RequestBody @Valid UpdateOffenderDetails offenderDetails) {
        smokeTestHelperService.updateOffenderDetails(crn, offenderDetails);
    }

}
