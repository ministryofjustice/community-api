package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentRelocateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentRelocateResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentRescheduleResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentUpdateResponse;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentRescheduleRequest;
import uk.gov.justice.digital.delius.service.AppointmentService;

@RestController
@Tag(name = "Appointments")
@PreAuthorize("hasRole('ROLE_COMMUNITY_INTERVENTIONS_UPDATE')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AppointmentBookingController {

    private final AppointmentService appointmentService;

    @RequestMapping(value = "/offenders/crn/{crn}/sentence/{sentenceId}/appointments/context/{contextName}",
                method = RequestMethod.POST,
                consumes = "application/json")
    @ApiResponses(
        value = {
                @ApiResponse(responseCode = "201", description = "Created"),
                @ApiResponse(responseCode = "400", description = "Invalid request"),
                @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
                @ApiResponse(responseCode = "409", description = "Conflicts with another appointment"),
                @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })

    @Operation(description = "Creates an Contact appointment for a specified context")
    public ResponseEntity<AppointmentCreateResponse> createAppointmentWithContextName(final @PathVariable("crn") String crn,
                                                                                      final @PathVariable("sentenceId") Long sentenceId,
                                                                                      final @Parameter(description = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                                          @PathVariable("contextName") String contextName,
                                                                                      final @RequestBody ContextlessAppointmentCreateRequest contextlessAppointmentCreateRequest) {

        AppointmentCreateResponse response = appointmentService.createAppointment(crn, sentenceId, contextName, contextlessAppointmentCreateRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/appointments/{appointmentId}/reschedule/context/{contextName}",
        method = RequestMethod.POST,
        consumes = "application/json")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })

    @Operation(description = "Reschedules an appointment")
    public AppointmentRescheduleResponse rescheduleAppointmentWithContextName(final @PathVariable("crn") String crn,
                                                                              final @PathVariable("appointmentId") Long appointmentId,
                                                                              final @Parameter(description = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                                    @PathVariable("contextName") String context,
                                                                              final @RequestBody ContextlessAppointmentRescheduleRequest appointmentRescheduleRequest) {

        return appointmentService.rescheduleAppointment(crn, appointmentId, context, appointmentRescheduleRequest);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/appointments/{appointmentId}/relocate",
        method = RequestMethod.POST,
        consumes = "application/json")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })

    @Operation(description = "Relocates an appointment")
    public AppointmentRelocateResponse relocateAppointment(final @PathVariable("crn") String crn,
                                                           final @PathVariable("appointmentId") Long appointmentId,
                                                           final @RequestBody AppointmentRelocateRequest appointmentRelocateRequest) {

        return appointmentService.relocateAppointment(crn, appointmentId, appointmentRelocateRequest);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/appointments/{appointmentId}/outcome/context/{contextName}",
        method = RequestMethod.POST,
        consumes = "application/json")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })

    @Operation(description = "Updates an Contact appointment outcome")
    public AppointmentUpdateResponse updateAppointmentOutcomeWithContext(final @PathVariable("crn") String crn,
                                                                         final @PathVariable("appointmentId") Long appointmentId,
                                                                         final @Parameter(description = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                             @PathVariable("contextName") String context,
                                                                         final @RequestBody ContextlessAppointmentOutcomeRequest appointmentOutcomeRequest) {

        return appointmentService.updateAppointmentOutcome(crn, appointmentId, context, appointmentOutcomeRequest);
    }
}
