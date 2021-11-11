package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.NotificationResponse;
import uk.gov.justice.digital.delius.service.NotificationService;

@RestController
@Api(tags = {"Contacts"})
@PreAuthorize("hasRole('ROLE_COMMUNITY_INTERVENTIONS_UPDATE')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class NotificationController {

    private NotificationService notificationService;

    @RequestMapping(value = "/offenders/crn/{crn}/sentences/{sentenceId}/notifications/context/{contextName}",
        method = RequestMethod.POST,
        consumes = "application/json")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "Notified", response = String.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(code = 409, message = "Conflicts with another appointment"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })

    @ApiOperation(value = "Creates a Contact Log for a specified context")
    public NotificationResponse notifyWithContextName(final @PathVariable("crn") String crn,
                                                                      final @PathVariable("sentenceId") Long sentenceId,
                                                                      final @ApiParam(value = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                           @PathVariable("contextName") String contextName,
                                                                      final @RequestBody ContextlessNotificationCreateRequest contextlessNotificationCreateRequest) {
        return notificationService.notifyCRSContact(crn, sentenceId, contextName, contextlessNotificationCreateRequest);
    }
}
