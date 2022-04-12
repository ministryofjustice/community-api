package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.data.api.StaffHuman;
import uk.gov.justice.digital.delius.data.api.TeamCreationResult;
import uk.gov.justice.digital.delius.service.CaseloadService;
import uk.gov.justice.digital.delius.service.TeamService;
import uk.gov.justice.digital.delius.validation.TeamCode;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

import static uk.gov.justice.digital.delius.data.api.CaseloadRole.OFFENDER_MANAGER;
import static uk.gov.justice.digital.delius.data.api.CaseloadRole.ORDER_SUPERVISOR;

@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Api(tags = "Teams")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
public class TeamResource {
    private final TeamService teamService;
    private final CaseloadService caseloadService;

    @RequestMapping(value = "teams/prisonOffenderManagers/create", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 400, message = "The custody request is invalid")
    })
    @ApiOperation(
        value = "Creates teams in each prison for prison offender managers. For each team the Unallocated staff member will also be created. Only teams or staff that are missing will be created. This only needs to run once per environment or when a new prison is added to Delius",
        tags = {"Teams", "Custody"},
        authorizations = {@Authorization("ROLE_COMMUNITY_CUSTODY_UPDATE")}
    )
    public TeamCreationResult createMissingPrisonOffenderManagerTeams() {
        return TeamCreationResult
            .builder()
            .teams(teamService.createMissingPrisonOffenderManagerTeams())
            .unallocatedStaff(teamService.createMissingPrisonOffenderManagerUnallocatedStaff())
            .build();
    }

    @GetMapping("/teams/{teamCode}/office-locations")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "All active office locations for the specified team", response = OfficeLocation.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 404, message = "The specified team does not exist or is not active"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ApiOperation(
        value = "Determines all active office locations for the specified team",
        authorizations = {@Authorization("ROLE_COMMUNITY")}
    )
    public List<OfficeLocation> getAllOfficeLocations(
        @PathVariable("teamCode")
        @ApiParam(value = "Team code", example = "N07T01")
        String teamCode
    ) {
        return teamService.getAllOfficeLocations(teamCode);
    }

    @GetMapping("/teams/{teamCode}/staff")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "All staff for the specified team", response = StaffDetails.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 404, message = "The specified team does not exist or is not active"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ApiOperation(
        value = "Determines all staff for the specified team",
        authorizations = {@Authorization("ROLE_COMMUNITY")}
    )
    public List<StaffDetails> getAllStaff(
        @PathVariable("teamCode")
        @ApiParam(value = "Team code", example = "N07T01")
            String teamCode
    ) {
        return teamService.getAllStaff(teamCode);
    }

    @ApiOperation(
        value = "Return the full caseload for all members of a probation team",
        notes = "Currently, this endpoint is restricted to offender managers and order supervisors. Additional management types (e.g. requirements, reports) may be added later.",
        authorizations = {@Authorization("ROLE_COMMUNITY")})
    @GetMapping(path = "/team/{teamCode}/caseload")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public Caseload getCaseloadForTeam(
        @ApiParam(name = "teamCode", example = "N07T01")
        @NotNull @TeamCode @PathVariable(value = "teamCode") final String teamCode) {
        return caseloadService.getCaseloadByTeamCode(teamCode, OFFENDER_MANAGER, ORDER_SUPERVISOR)
            .orElseThrow(() -> new NotFoundException(String.format("Team with code %s", teamCode)));
    }

    @ApiOperation(
        value = "Return the managed offenders for all members of a probation team",
        authorizations = {@Authorization("ROLE_COMMUNITY")})
    @GetMapping(path = "/team/{teamCode}/caseload/managedOffenders")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public Set<ManagedOffenderCrn> getCaseloadOffendersForTeam(
        @ApiParam(name = "teamCode", example = "N07T01")
        @NotNull @TeamCode @PathVariable(value = "teamCode") final String teamCode) {
        return caseloadService.getCaseloadByTeamCode(teamCode, OFFENDER_MANAGER)
            .map(Caseload::getManagedOffenders)
            .orElseThrow(() -> new NotFoundException(String.format("Team with code %s", teamCode)));
    }

    @ApiOperation(
        value = "Return the supervised orders/events for all members of a probation team",
        authorizations = {@Authorization("ROLE_COMMUNITY")})
    @GetMapping(path = "/team/{teamCode}/caseload/supervisedOrders")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public Set<ManagedEventId> getCaseloadOrdersForTeam(
        @ApiParam(name = "teamCode", example = "N07T01")
        @NotNull @TeamCode @PathVariable(value = "teamCode") final String teamCode) {
        return caseloadService.getCaseloadByTeamCode(teamCode, ORDER_SUPERVISOR)
            .map(Caseload::getSupervisedOrders)
            .orElseThrow(() -> new NotFoundException(String.format("Team with code %s", teamCode)));
    }

}
