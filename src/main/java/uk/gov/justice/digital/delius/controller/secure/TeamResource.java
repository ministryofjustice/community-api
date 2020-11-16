package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.TeamCreationResult;
import uk.gov.justice.digital.delius.service.TeamService;

@RestController
@Slf4j
@Api(tags = {"Custody"}, authorizations = {@Authorization("ROLE_COMMUNITY_CUSTODY_UPDATE")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
public class TeamResource {
    private final TeamService teamService;

    public TeamResource(TeamService teamService) {
        this.teamService = teamService;
    }


    @RequestMapping(value = "teams/prisonOffenderManagers/create", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 400, message = "The custody request is invalid")
    })
    @ApiOperation(value = "Creates teams in each prison for prison offender managers. Only teams that are missing will be created. This only needs to run once per environment or when a new prison is added to Delius")
    public TeamCreationResult createMissingPrisonOffenderManagerTeams() {
        return teamService.createMissingPrisonOffenderManagerTeams();
    }

}
