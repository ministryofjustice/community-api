package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Caseload;
import uk.gov.justice.digital.delius.data.api.ManagedOffenderCrn;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.service.CaseloadService;
import uk.gov.justice.digital.delius.service.TeamService;
import uk.gov.justice.digital.delius.validation.TeamCode;

import java.util.List;
import java.util.Set;

import static uk.gov.justice.digital.delius.data.api.CaseloadRole.OFFENDER_MANAGER;

@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "Teams")
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
            @ApiResponse(responseCode = "200", description = "All staff for the specified team"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(responseCode = "404", description = "The specified team does not exist or is not active"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @Operation(description = "Determines all staff for the specified team. Requires ROLE_COMMUNITY")
    public List<StaffDetails> getAllStaff(
        @PathVariable("teamCode")
        @Parameter(description = "Team code", example = "N07T01")
            String teamCode
    ) {
        return teamService.getAllStaff(teamCode);
    }

    @Operation(description = "Return the managed offenders for all members of a probation team. Requires ROLE_COMMUNITY")
    @GetMapping(path = "/team/{teamCode}/caseload/managedOffenders")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public Set<ManagedOffenderCrn> getCaseloadOffendersForTeam(
        @Parameter(name = "teamCode", example = "N07T01")
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
