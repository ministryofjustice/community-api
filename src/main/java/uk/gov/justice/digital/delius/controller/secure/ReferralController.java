package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ReferralSentRequest;
import uk.gov.justice.digital.delius.service.ReferralService;

import javax.validation.Valid;

@RestController
@Api(tags = {"Referrals"})
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @RequestMapping(value = "/offenders/crn/{crn}/referral/sent",
                    method = RequestMethod.POST,
                    consumes = "application/json")
    @ApiResponses(
            value = {
                @ApiResponse(code = 201, message = "Created", response = String.class),
                @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
                @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })

    @ApiOperation(value = "Adds a sent referral contact entry to the contact log")
    public ResponseEntity<String> createReferralSent(final @PathVariable("crn") String crn,
                                                     final @RequestBody @Valid ReferralSentRequest referralSentRequest) {
        return referralService.createReferralSent(crn, referralSentRequest);
    }
}
