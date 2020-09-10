package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.OffenderDelta;
import uk.gov.justice.digital.delius.service.OffenderUpdatesService;

@Api(tags = "Offender update resource (Secure) for retrieving updates to offenders")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class OffenderUpdatesResource {
    private final OffenderUpdatesService offenderUpdatesService;

    @ApiOperation(
            value = "Returns the next update required processing for any offender", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "No updates found", response = ErrorResponse.class),
                    @ApiResponse(code = 409, message = "Attempt to retrieve the latest update that is already in progress", response = ErrorResponse.class)
            })
    @GetMapping(value = "offenders/nextUpdate")
    public OffenderDelta getNextOffenderUpdate() {
        return null;
    }


}
