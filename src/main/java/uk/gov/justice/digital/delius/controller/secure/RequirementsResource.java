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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.data.api.LicenceConditions;
import uk.gov.justice.digital.delius.data.api.PssRequirements;
import uk.gov.justice.digital.delius.service.RequirementService;


@Api(tags = "Sentence requirements and breach", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class RequirementsResource {
    private RequirementService requirementsService;

    @ApiOperation(value = "Returns the Post Sentence Supervision Requirements for a conviction")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/pssRequirements")
    public PssRequirements getPssRequirementsByConvictionId(
            @PathVariable(value = "crn") String crn,
            @PathVariable(value = "convictionId") Long convictionId
    ) {
        return requirementsService.getPssRequirementsByConvictionId(crn, convictionId);
    }

    @ApiOperation(value = "Returns the Licence Conditions for a conviction")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/licenceConditions")
    public LicenceConditions getLicenceConditionsByConvictionId(
            @PathVariable(value = "crn") String crn,
            @PathVariable(value = "convictionId") Long convictionId
    ) {
        return requirementsService.getLicenceConditionsByConvictionId(crn, convictionId);
    }

    @ApiOperation(value = "Returns the requirements for a conviction")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not Found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/requirements")
    public ConvictionRequirements getRequirementsByConvictionId(
            @PathVariable(value = "crn") String crn,
            @PathVariable(value = "convictionId") Long convictionId,
            @ApiParam(name = "activeOnly", value = "retrieve only active requirements", example = "true")
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly
    ) {
        if(activeOnly){
            return requirementsService.getActiveRequirementsByConvictionId(crn, convictionId);
        }
        return requirementsService.getRequirementsByConvictionId(crn, convictionId);
    }
}
