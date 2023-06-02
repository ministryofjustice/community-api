package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.service.CaseloadService;
import uk.gov.justice.digital.delius.service.StaffService;

import java.util.List;
import java.util.Set;

import static uk.gov.justice.digital.delius.data.api.CaseloadRole.OFFENDER_MANAGER;

@Slf4j
@Tag(name = "Staff", description = "Requires ROLE_COMMUNITY")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@Validated
public class StaffResource {

    private final StaffService staffService;
    private final CaseloadService caseloadService;

    @Operation(description = "Return list of of currently managed offenders for one responsible officer (RO). Accepts a Delius staff officer identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
    })
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}/managedOffenders")
    public List<ManagedOffender> getOffendersForResponsibleOfficerIdentifier(
        @Parameter(name = "staffIdentifier", description = "Delius officer identifier of the responsible officer", example = "123456", required = true) @NotNull @PathVariable(value = "staffIdentifier") final Long staffIdentifier,
        @Parameter(name = "current", description = "Current only", example = "false") @RequestParam(name = "current", required = false, defaultValue = "false") final boolean current) {
        return staffService.getManagedOffendersByStaffIdentifier(staffIdentifier, current)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %d", staffIdentifier)));
    }

    @Operation(description = "Return details of a staff member including option user details. Accepts a Delius staff officer identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
    })
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}")
    public StaffDetails getStaffDetailsForStaffIdentifier(
        @Parameter(name = "staffIdentifier", description = "Delius officer identifier", example = "123456", required = true)
        @NotNull
        @PathVariable(value = "staffIdentifier") final long staffIdentifier) {
        return staffService.getStaffDetailsByStaffIdentifier(staffIdentifier)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %s", staffIdentifier)));
    }

    @Operation(description = "Return details of a staff member including user details. Accepts a Delius staff username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
    })
    @GetMapping(path = "/staff/username/{username}")
    public StaffDetails getStaffDetailsForUsername(
        @Parameter(name = "username", description = "Delius username", example = "SheliaHancockNPS", required = true)
        @NotNull
        @PathVariable(value = "username") final String username) {
        return staffService.getStaffDetailsByUsername(username)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with username %s", username)));
    }

    @Operation(description = "Return details of a staff member including user details. Accepts a Delius staff code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
    })
    @GetMapping(path = "/staff/staffCode/{staffCode}")
    public StaffDetails getStaffDetailsForStaffCode(
        @Parameter(name = "staffCode", description = "Delius staff code", example = "X12345", required = true)
        @NotNull
        @PathVariable(value = "staffCode") final String staffCode) {
        return staffService.getStaffDetailsByStaffCode(staffCode)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with staffCode %s", staffCode)));
    }

    @Operation(description = "Returns a list of staff details for supplied usernames - POST version to allow large user lists. staff details for supplied usernames", summary = "getStaffDetailsList")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
    })
    @PostMapping(path = "/staff/list", consumes = "application/json")
    public List<StaffDetails> getStaffDetailsList(final @RequestBody Set<String> usernames) {
        return staffService.getStaffDetailsByUsernames(usernames);
    }

    @Operation(description = "Returns a list of staff details for supplied staffCodes - POST version to allow large user lists. staff details for supplied staffCodes", summary = "getStaffDetailsByStaffCodeList")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
    })
    @PostMapping(path = "/staff/list/staffCodes", consumes = "application/json")
    public List<StaffDetails> getStaffDetailsByStaffCodeList(final @RequestBody Set<String> staffCodes) {
        return staffService.getStaffDetailsByStaffCodes(staffCodes);
    }

    @Operation(description = "Return the full caseload for a probation staff/officer, returning only the managed offenders")
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}/caseload/managedOffenders")
    public Set<ManagedOffenderCrn> getCaseloadOffendersForStaff(
        @Parameter(name = "staffIdentifier", description = "Delius staff/officer identifier", example = "123456", required = true)
        @NotNull @PathVariable(value = "staffIdentifier") final Long staffIdentifier) {
        return caseloadService.getCaseloadByStaffIdentifier(staffIdentifier, OFFENDER_MANAGER)
            .map(Caseload::getManagedOffenders)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %d", staffIdentifier)));
    }

    @Operation(description = "Return the list of heads of a specific probation delivery unit (aka borough)")
    @GetMapping(path = "/staff/pduHeads/{pduCode}")
    public List<StaffDetails> getProbationPduHeads(@NotNull @PathVariable String pduCode) {
        return staffService.getProbationDeliveryUnitHeads(pduCode)
            .orElseThrow(() -> new NotFoundException(String.format("Probation delivery unit with code %s", pduCode)));
    }
}
