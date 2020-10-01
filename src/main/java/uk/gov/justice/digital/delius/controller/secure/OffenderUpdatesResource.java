package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.OffenderUpdate;
import uk.gov.justice.digital.delius.service.OffenderUpdatesService;

@Api(tags = "Offender update resource (Secure) for retrieving updates to offenders")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY_EVENTS')")
public class OffenderUpdatesResource {
    private final OffenderUpdatesService offenderUpdatesService;

    @ApiOperation(
            value = "Returns the next update for any offender. If none, will look for failed updates and set them in progress again", notes = "requires ROLE_COMMUNITY_EVENTS")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "No updates found", response = ErrorResponse.class),
                    @ApiResponse(code = 409, message = "Attempt to retrieve the latest update that is already in progress", response = ErrorResponse.class)
            })
    @GetMapping(value = "offenders/nextUpdate")
    public OffenderUpdate getAndLockNextOffenderUpdate() {
        return offenderUpdatesService.getAndLockNextUpdate().orElseThrow(() -> new NotFoundException("No updates found"));
    }

    @ApiOperation(
            value = "Deletes an update of an offender previous retrieved by `/offenders/nextUpdate` ", notes = "requires ROLE_COMMUNITY_EVENTS")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Update not found", response = ErrorResponse.class),
            })
    @DeleteMapping(value = "offenders/update/{offenderDeltaId}")
    public void deleteOffenderUpdate(@PathVariable Long offenderDeltaId) {
        offenderUpdatesService.deleteUpdate(offenderDeltaId);
    }

    @ApiOperation(
            value = "Mark an offender update as failed", notes = "requires ROLE_COMMUNITY_EVENTS")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Update not found", response = ErrorResponse.class),
            })
    @PutMapping(value = "offenders/update/{offenderDeltaId}/markAsFailed")
    public void markAsFailed(@PathVariable Long offenderDeltaId) {
        offenderUpdatesService.markAsFailed(offenderDeltaId);
    }


}
