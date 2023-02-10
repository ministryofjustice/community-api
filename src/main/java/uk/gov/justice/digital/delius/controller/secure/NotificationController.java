package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.NotificationResponse;
import uk.gov.justice.digital.delius.service.NotificationService;

@RestController
@Tag(name = "Contacts")
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
            @ApiResponse(responseCode = "200", description = "Notified"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(responseCode = "409", description = "Conflicts with another appointment"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })

    @Operation(description = "Creates a Contact Log for a specified context")
    public NotificationResponse notifyWithContextName(final @PathVariable("crn") String crn,
                                                                      final @PathVariable("sentenceId") Long sentenceId,
                                                                      final @Parameter(description = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                           @PathVariable("contextName") String contextName,
                                                                      final @RequestBody ContextlessNotificationCreateRequest contextlessNotificationCreateRequest) {
        return notificationService.notifyCRSContact(crn, sentenceId, contextName, contextlessNotificationCreateRequest);
    }
}
