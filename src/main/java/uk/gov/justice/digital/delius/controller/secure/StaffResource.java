package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.service.StaffService;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Api(description = "Staff resources protected by OAUTH2", tags = "Staff (Secure)")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class StaffResource {

    private final StaffService staffService;

    @ApiOperation(
            value = "Return list of of currently managed offenders for one responsible officer (RO)",
            notes = "Accepts a Delius staff officer code",
            authorizations = {@Authorization("ROLE_COMMUNITY")},
            nickname = "getOffendersForResponsibleOfficer")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = ManagedOffender.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/staff/staffCode/{staffCode}/managedOffenders")
    @NationalUserOverride
    public ResponseEntity<List<ManagedOffender>> getOffendersForResponsibleOfficer(
            @ApiParam(name = "staffCode", value = "Delius officer code of the responsible officer", example = "ASPD956", required = true)
            @NotNull @PathVariable(value = "staffCode") final String staffCode,
            @ApiParam(name = "current", value = "Current only", example = "false")  @RequestParam(name = "current", required = false, defaultValue = "false") final boolean current) {
        return staffService.getManagedOffendersByStaffCode(staffCode, current)
                .map(managedOffenders -> new ResponseEntity<>(managedOffenders ,OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }


}

