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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Smoke test")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_SMOKE_TEST')")
@ConditionalOnProperty(name = "smoke.test.aware", havingValue = "true")
public class SmokeTestHelperResource {
    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/smoketest/custody/reset", method = RequestMethod.POST, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_SMOKE_TEST"),
            @ApiResponse(code = 404, message = "Either the requested offender was not found or no active custodial sentences were found")
    })
    @ApiOperation(value = "Resets custody data to the state before a Delius offender record has been matched to a NOMIS record")
    public void resetCustodySmokeTestData(@PathVariable String nomsNumber) {

    }

}
