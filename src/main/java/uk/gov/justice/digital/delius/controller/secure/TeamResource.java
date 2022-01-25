package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
import uk.gov.justice.digital.delius.data.api.TeamCreationResult;
import uk.gov.justice.digital.delius.data.api.TeamManagedOffender;
import uk.gov.justice.digital.delius.service.TeamService;

import java.util.List;

@RestController
@Slf4j
@Api
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
public class TeamResource {
    private final TeamService teamService;

    public TeamResource(TeamService teamService) {
        this.teamService = teamService;
    }


    @RequestMapping(value = "teams/prisonOffenderManagers/create", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 400, message = "The custody request is invalid")
    })
    @ApiOperation(
        value = "Creates teams in each prison for prison offender managers. For each team the Unallocated staff member will also be created. Only teams or staff that are missing will be created. This only needs to run once per environment or when a new prison is added to Delius",
        tags = {"Custody"},
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
        tags = {"Teams"},
        authorizations = {@Authorization("ROLE_COMMUNITY")}
    )
    public List<OfficeLocation> getAllOfficeLocations(
        @PathVariable("teamCode")
        @ApiParam(value = "Team code", example = "N07T01")
        String teamCode
    ) {
        return teamService.getAllOfficeLocations(teamCode);
    }

    @GetMapping("/teams/managedOffenders")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "All active office locations for the specified team", response = OfficeLocation.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 404, message = "The specified team does not exist or is not active"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ApiOperation(
        value = "EXPERIMENTAL: Given a list of team codes find the offenders being managed and details of the staff who are managing them.",
        tags = {"Teams"},
        authorizations = {@Authorization("ROLE_COMMUNITY")}
    )
    public List<TeamManagedOffender> getManagedOffendersForTeams(
        @RequestParam(name = "teamCode") List<String> teamCodes,
        @RequestParam(name = "current", required = false, defaultValue = "true") boolean current
    ) {
        return teamService.getManagedOffendersForTeams(teamCodes, current);
    }
}
