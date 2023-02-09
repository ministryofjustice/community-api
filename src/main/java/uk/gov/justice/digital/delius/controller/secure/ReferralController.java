package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralEndRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralStartRequest;
import uk.gov.justice.digital.delius.data.api.ReferralEndResponse;
import uk.gov.justice.digital.delius.data.api.ReferralStartResponse;
import uk.gov.justice.digital.delius.service.ReferralService;

@RestController
@Tag(name = "Referrals")
@PreAuthorize("hasRole('ROLE_COMMUNITY_INTERVENTIONS_UPDATE')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @RequestMapping(value = "/offenders/crn/{crn}/referral/start/context/{context}",
        method = RequestMethod.POST,
        consumes = "application/json")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })

    @Operation(description = "Starts an NSI referral")
    public ReferralStartResponse startReferralContextLess(final @PathVariable("crn") String crn,
                                                          final @Parameter(description = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                @PathVariable("context") String context,
                                                          final @RequestBody @Valid ContextlessReferralStartRequest referralStartRequest) {
        return referralService.startNsiReferral(crn, context, referralStartRequest);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/referral/end/context/{context}",
        method = RequestMethod.POST,
        consumes = "application/json")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })

    @Operation(description = "Ends a NSI referral")
    public ReferralEndResponse endReferralContextLess(final @PathVariable("crn") String crn,
                                                      final @Parameter(description = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                            @PathVariable("context") String context,
                                                      final @RequestBody @Valid ContextlessReferralEndRequest referralEndRequest) {
        return referralService.endNsiReferral(crn, context, referralEndRequest);
    }
}
