package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.service.StaffService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Slf4j
@Api(tags = "Staff", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class StaffResource {

    private final StaffService staffService;

    @ApiOperation(value = "Return list of of currently managed offenders for one responsible officer (RO)", notes = "Accepts a Delius staff officer identifier")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ManagedOffender.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}/managedOffenders")
    public List<ManagedOffender> getOffendersForResponsibleOfficerIdentifier(
            @ApiParam(name = "staffIdentifier", value = "Delius officer identifier of the responsible officer", example = "123456", required = true) @NotNull @PathVariable(value = "staffIdentifier") final Long staffIdentifier,
            @ApiParam(name = "current", value = "Current only", example = "false") @RequestParam(name = "current", required = false, defaultValue = "false") final boolean current) {
        return staffService.getManagedOffendersByStaffIdentifier(staffIdentifier, current)
                .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %d", staffIdentifier)));
    }

    @ApiOperation(value = "Return details of a staff member including option user details", notes = "Accepts a Delius staff officer identifier")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffDetails.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}")
    public StaffDetails getStaffDetailsForStaffIdentifier(
            @ApiParam(name = "staffIdentifier", value = "Delius officer identifier", example = "123456", required = true)
            @NotNull
            @PathVariable(value = "staffIdentifier") final long staffIdentifier) {
        log.info("getStaffDetailsForStaffIdentifier called with {}", staffIdentifier);
        return staffService.getStaffDetailsByStaffIdentifier(staffIdentifier)
                .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %s", staffIdentifier)));
    }

    @ApiOperation(value = "Return details of a staff member including user details", notes = "Accepts a Delius staff username")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffDetails.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @GetMapping(path = "/staff/username/{username}")
    public StaffDetails getStaffDetailsForUsername(
            @ApiParam(name = "username", value = "Delius username", example = "SheliaHancockNPS", required = true)
            @NotNull
            @PathVariable(value = "username") final String username) {
        log.info("getStaffDetailsByUsername called with {}", username);
        return staffService.getStaffDetailsByUsername(username)
                .orElseThrow(() -> new NotFoundException(String.format("Staff member with username %s", username)));
    }

    @ApiOperation(value = "Returns a list of staff details for supplied usernames - POST version to allow large user lists.", notes = "staff details for supplied usernames", nickname = "getStaffDetailsList")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = StaffDetails.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @PostMapping(path="/staff/list", consumes = "application/json")
    public List<StaffDetails> getStaffDetailsList(final @RequestBody Set<String> usernames){
        log.info("getStaffDetailsList called with {}", usernames);
        return staffService.getStaffDetailsByUsernames(usernames);
    }
}
