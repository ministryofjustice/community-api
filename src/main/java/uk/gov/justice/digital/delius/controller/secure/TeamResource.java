package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedEventId;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.data.api.OfficeLocation;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.data.api.TeamCreationResult;
import uk.gov.justice.digital.delius.service.CaseloadService;
import uk.gov.justice.digital.delius.service.TeamService;
import uk.gov.justice.digital.delius.validation.TeamCode;

import jakarta.validation.constraints.NotNull;
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
    public static final Sort CASELOAD_DEFAULT_SORT = Sort.by("allocationDate").descending();
    public static final String DEFAULT_PAGE_SIZE = "2147483647"; // Integer.MAX_VALUE
    private final TeamService teamService;
    private final CaseloadService caseloadService;

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
        value = "Return the managed offenders for all members of a probation team",
        authorizations = {@Authorization("ROLE_COMMUNITY")})
    @GetMapping(path = "/team/{teamCode}/caseload/managedOffenders")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public Set<ManagedOffenderCrn> getCaseloadOffendersForTeam(
        @ApiParam(name = "teamCode", example = "N07T01")
        @NotNull @TeamCode @PathVariable(value = "teamCode") final String teamCode,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize
    ) {
        return caseloadService.getCaseloadByTeamCode(
                teamCode,
                PageRequest.of(page, pageSize, CASELOAD_DEFAULT_SORT),
                OFFENDER_MANAGER
            ).map(Caseload::getManagedOffenders)
            .orElseThrow(() -> teamNotFound(teamCode));
    }

    private NotFoundException teamNotFound(String teamCode) {
        return new NotFoundException(String.format("Team with code %s", teamCode));
    }
}
