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
        log.info("getStaffDetailsByUsername called with {}", username);
        return staffService.getStaffDetailsByUsername(username)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with username %s", username)));
    }

    @ApiOperation(value = "Returns a list of staff details for supplied usernames - POST version to allow large user lists.", notes = "staff details for supplied usernames", nickname = "getStaffDetailsList")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @PostMapping(path = "/staff/list", consumes = "application/json")
    public List<StaffDetails> getStaffDetailsList(final @RequestBody Set<String> usernames) {
        log.info("getStaffDetailsList called with {}", usernames);
        return staffService.getStaffDetailsByUsernames(usernames);
    }

    @ApiOperation(value = "EXPERIMENTAL: Return list of of currently managed offenders, with RAR requirement and only a single active sentence",
        notes = "Accepts a Delius Username. No backward compatibility guaranteed - intended for the use of the Manage a Supervision service, behaviour or responses may be modified in the future.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)})
    @GetMapping(path = "/staff/username/{username}/manage-supervisions-eligible-offenders")
    public Page<StaffCaseloadEntry> getManageSupervisionsEligibleOffenders(
        @ApiParam(name = "username", value = "Delius username", example = "SheliaHancockNPS", required = true)
        @NotNull
        @PathVariable(value = "username") final String username,
        @PageableDefault(sort = {"secondName", "firstName"}, direction = Direction.ASC, size = Integer.MAX_VALUE) final Pageable pageable) {
        return staffService.getManageSupervisionsEligibleOffendersByUsername(username, pageable);
    }

    @ApiOperation(
        value = "Return the full caseload for a probation staff/officer",
        notes = "Currently, this endpoint is restricted to offender managers and order supervisors. Additional management types (e.g. requirements, reports) may be added later.")
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}/caseload")
    public Caseload getCaseloadForStaff(
        @ApiParam(name = "staffIdentifier", value = "Delius staff/officer identifier", example = "123456", required = true)
        @NotNull @PathVariable(value = "staffIdentifier") final Long staffIdentifier) {
        return caseloadService.getCaseloadByStaffIdentifier(staffIdentifier, OFFENDER_MANAGER, ORDER_SUPERVISOR)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %d", staffIdentifier)));
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

    @ApiOperation(value = "Return the full caseload for a probation staff/officer, returning only the supervised orders/events")
    @GetMapping(path = "/staff/staffIdentifier/{staffIdentifier}/caseload/supervisedOrders")
    public Set<ManagedEventId> getCaseloadOrdersForStaff(
        @ApiParam(name = "staffIdentifier", value = "Delius staff/officer identifier", example = "123456", required = true)
        @NotNull @PathVariable(value = "staffIdentifier") final Long staffIdentifier) {
        return caseloadService.getCaseloadByStaffIdentifier(staffIdentifier, ORDER_SUPERVISOR)
            .map(Caseload::getSupervisedOrders)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with identifier %d", staffIdentifier)));
    }

    @ApiOperation(
        value = "Return the full caseload for a probation staff/officer",
        notes = "Currently, this endpoint is restricted to offender managers and order supervisors. Additional management types (e.g. requirements, reports) may be added later.")
    @GetMapping(path = "/staff/staffCode/{staffCode}/caseload")
    public Caseload getCaseloadForStaff(
        @ApiParam(name = "staffCode", value = "Delius staff/officer code", example = "N01A123", required = true)
        @NotNull @StaffCode @PathVariable(value = "staffCode") final String staffCode) {
        return getCaseloadForStaff(staffService.getStaffIdByStaffCode(staffCode)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with code %s", staffCode))));
    }

    @ApiOperation(value = "Return the full caseload for a probation staff/officer, returning only the managed offenders")
    @GetMapping(path = "/staff/staffCode/{staffCode}/caseload/managedOffenders")
    public Set<ManagedOffenderCrn> getCaseloadOffendersForStaff(
        @ApiParam(name = "staffCode", value = "Delius staff/officer code", example = "N01A123", required = true)
        @NotNull @StaffCode @PathVariable(value = "staffCode") final String staffCode) {
        return getCaseloadOffendersForStaff(staffService.getStaffIdByStaffCode(staffCode)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with code %s", staffCode))));
    }

    @ApiOperation(value = "Return the full caseload for a probation staff/officer, returning only the supervised orders/events")
    @GetMapping(path = "/staff/staffCode/{staffCode}/caseload/supervisedOrders")
    public Set<ManagedEventId> getCaseloadOrdersForStaff(
        @ApiParam(name = "staffCode", value = "Delius staff/officer code", example = "N01A123", required = true)
        @NotNull @StaffCode @PathVariable(value = "staffCode") final String staffCode) {
        return getCaseloadOrdersForStaff(staffService.getStaffIdByStaffCode(staffCode)
            .orElseThrow(() -> new NotFoundException(String.format("Staff member with code %s", staffCode))));
    }

}
