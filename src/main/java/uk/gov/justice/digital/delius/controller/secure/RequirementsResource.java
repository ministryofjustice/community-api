package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.data.api.LicenceConditions;
import uk.gov.justice.digital.delius.data.api.PssRequirements;
import uk.gov.justice.digital.delius.service.RequirementService;


@Tag(name = "Sentence requirements and breach", description = "Requires ROLE_COMMUNITY")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_COMMUNITY','ROLE_PROBATION_INTEGRATION_ADMIN')")
public class RequirementsResource {
    private RequirementService requirementsService;

    @Operation(description = "Returns the Post Sentence Supervision Requirements for a conviction")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "Not Found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/pssRequirements")
    public PssRequirements getPssRequirementsByConvictionId(
            @PathVariable(value = "crn") String crn,
            @PathVariable(value = "convictionId") Long convictionId
    ) {
        return requirementsService.getPssRequirementsByConvictionId(crn, convictionId);
    }

    @Operation(description = "Returns the Licence Conditions for a conviction")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "Not Found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/licenceConditions")
    public LicenceConditions getLicenceConditionsByConvictionId(
            @PathVariable(value = "crn") String crn,
            @PathVariable(value = "convictionId") Long convictionId
    ) {
        return requirementsService.getLicenceConditionsByConvictionId(crn, convictionId);
    }

    @Operation(description = "Returns the requirements for a conviction")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "Not Found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/requirements")
    public ConvictionRequirements getRequirementsByConvictionId(
            @PathVariable(value = "crn") String crn,
            @PathVariable(value = "convictionId") Long convictionId,
            @Parameter(name = "activeOnly", description = "retrieve only active requirements", example = "true")
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly,
            @Parameter(name = "excludeSoftDeleted", description = "retrieve only requirements that have not been soft-deleted", example = "true")
            @RequestParam(name = "excludeSoftDeleted", required = false, defaultValue = "false") final boolean excludeSoftDeleted
    ) {
        return requirementsService.getRequirementsByConvictionId(crn, convictionId, !activeOnly, !excludeSoftDeleted);
    }
}
