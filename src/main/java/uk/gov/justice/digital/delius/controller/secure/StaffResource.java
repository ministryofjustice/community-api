package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.data.api.StaffCaseloadEntry;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.service.CaseloadService;
import uk.gov.justice.digital.delius.service.StaffService;
import uk.gov.justice.digital.delius.validation.StaffCode;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

import static uk.gov.justice.digital.delius.data.api.CaseloadRole.OFFENDER_MANAGER;
import static uk.gov.justice.digital.delius.data.api.CaseloadRole.ORDER_SUPERVISOR;

@Slf4j
@Api(tags = "Staff", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@Validated
public class StaffResource {

    private final StaffService staffService;
    private final CaseloadService caseloadService;

    @ApiOperation(value = "Return list of of currently managed offenders for one responsible officer (RO)", notes = "Accepts a Delius staff officer identifier")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
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
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
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
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @GetMapping(path = "/staff/username/{username}")
    public StaffDetails getStaffDetailsForUsername(
        @ApiParam(name = "username", value = "Delius username", example = "SheliaHancockNPS", required = true)
        @NotNull
        @PathVariable(value = "username") final String username) {
        return staffService.getStaffDetailsByUsername(username)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with username %s", username)));
    }

    @ApiOperation(value = "Return details of a staff member including user details", notes = "Accepts a Delius staff code")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @GetMapping(path = "/staff/staffCode/{staffCode}")
    public StaffDetails getStaffDetailsForStaffCode(
        @ApiParam(name = "staffCode", value = "Delius staff code", example = "X12345", required = true)
        @NotNull
        @PathVariable(value = "staffCode") final String staffCode) {
        return staffService.getStaffDetailsByStaffCode(staffCode)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with staffCode %s", staffCode)));
    }

    @ApiOperation(value = "Returns a list of staff details for supplied usernames - POST version to allow large user lists.", notes = "staff details for supplied usernames", nickname = "getStaffDetailsList")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @PostMapping(path = "/staff/list", consumes = "application/json")
    public List<StaffDetails> getStaffDetailsList(final @RequestBody Set<String> usernames) {
        return staffService.getStaffDetailsByUsernames(usernames);
    }

    @ApiOperation(value = "Returns a list of staff details for supplied staffCodes - POST version to allow large user lists.", notes = "staff details for supplied staffCodes", nickname = "getStaffDetailsByStaffCodeList")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @PostMapping(path = "/staff/list/staffCodes", consumes = "application/json")
    public List<StaffDetails> getStaffDetailsByStaffCodeList(final @RequestBody Set<String> staffCodes) {
        return staffService.getStaffDetailsByStaffCodes(staffCodes);
    }

    @ApiOperation(value = "Return the full caseload for a probation staff/officer, returning only the managed offenders")
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}/caseload/managedOffenders")
    public Set<ManagedOffenderCrn> getCaseloadOffendersForStaff(
        @ApiParam(name = "staffIdentifier", value = "Delius staff/officer identifier", example = "123456", required = true)
        @NotNull @PathVariable(value = "staffIdentifier") final Long staffIdentifier) {
        return caseloadService.getCaseloadByStaffIdentifier(staffIdentifier, OFFENDER_MANAGER)
            .map(Caseload::getManagedOffenders)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %d", staffIdentifier)));
    }

    @ApiOperation(value = "Return the full caseload for a probation staff/officer, returning only the managed offenders")
    @GetMapping(path = "/staff/staffCode/{staffCode}/caseload/managedOffenders")
    public Set<ManagedOffenderCrn> getCaseloadOffendersForStaff(
        @ApiParam(name = "staffCode", value = "Delius staff/officer code", example = "N01A123", required = true)
        @NotNull @StaffCode @PathVariable(value = "staffCode") final String staffCode) {
        return getCaseloadOffendersForStaff(staffService.getStaffIdByStaffCode(staffCode)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with code %s", staffCode))));
    }

    @ApiOperation(value = "Return the list of heads of a specific probation delivery unit (aka borough)")
    @GetMapping(path = "/staff/pduHeads/{pduCode}")
    public List<StaffDetails> getProbationPduHeads(@NotNull @PathVariable String pduCode) {
        return staffService.getProbationDeliveryUnitHeads(pduCode)
            .orElseThrow(() -> new NotFoundException(String.format("Probation delivery unit with code %s", pduCode)));
    }
}
