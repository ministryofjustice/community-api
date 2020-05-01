package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.jpa.dao.OffenderDelta;
import uk.gov.justice.digital.delius.service.OffenderDeltaService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "Offender Events (Secure)", value = "Low level API for propagating significant events", authorizations = {@Authorization("ROLE_PROBATION_OFFENDER_EVENTS")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_PROBATION_OFFENDER_EVENTS')")
public class OffenderDeltaControllerSecure {

    private final OffenderDeltaService offenderDeltaService;

    @ApiOperation(
            value = "Returns a list of offender IDs which have be inserted/updated or deleted")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/offenderDeltaIds")
    public List<OffenderDelta> getOffenderDeltas() {
        log.info("Call to getOffenderDeltas");
        return offenderDeltaService.findAll();
    }

    @ApiOperation(
            value = "Deletes delta data before the date supplied")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @DeleteMapping(value = "/offenderDeltaIds")
    @ResponseStatus(HttpStatus.OK)
    public void deleteOffenderDeltas(@ApiParam(name = "before", value = "Delete records before the date time provided", example = "2019-03-02T16:45:00.000Z", required = true)
                                     final @RequestParam("before") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        log.info("Call to deleteOffenderDeltas before {}", before.toString());
        offenderDeltaService.deleteBefore(before);
    }
}
