package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.service.RequirementService;

import javax.validation.constraints.NotNull;
import java.util.Collections;


@Api(tags = "Requirements resources (Secure)", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class RequirementsResource {
    private RequirementService requirementsService;

    @ApiOperation(value = "Returns the requirements for a conviction")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = Contact.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}/requirements")
    public ConvictionRequirements getRequirementsByConvictionId(
            @PathVariable(value = "crn") String crn,
            @PathVariable(value = "convictionId") Long convictionId
    ) {
        return requirementsService.getRequirementsByConvictionId(crn, convictionId);
    }
}
