package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentRescheduleResponse;
import uk.gov.justice.digital.delius.data.api.AppointmentUpdateResponse;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentRescheduleRequest;
import uk.gov.justice.digital.delius.service.AppointmentService;

@RestController
@Api(tags = {"Appointments"})
@PreAuthorize("hasRole('ROLE_COMMUNITY_INTERVENTIONS_UPDATE')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AppointmentBookingController {

    private final AppointmentService appointmentService;

    @RequestMapping(value = "/offenders/crn/{crn}/sentence/{sentenceId}/appointments",
                    method = RequestMethod.POST,
                    consumes = "application/json")
    @ApiResponses(
            value = {
                @ApiResponse(code = 201, message = "Created", response = String.class),
                @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
                @ApiResponse(code = 409, message = "Conflicts with another appointment"),
                @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })

    @ApiOperation(value = "Creates an Contact appointment")
    public ResponseEntity<AppointmentCreateResponse> createAppointment(final @PathVariable("crn") String crn,
                                                                       final @PathVariable("sentenceId") Long sentenceId,
                                                                       final @RequestBody AppointmentCreateRequest appointmentCreateRequest) {

        AppointmentCreateResponse response = appointmentService.createAppointment(crn, sentenceId, appointmentCreateRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/sentence/{sentenceId}/appointments/context/{contextName}",
                method = RequestMethod.POST,
                consumes = "application/json")
    @ApiResponses(
        value = {
                @ApiResponse(code = 201, message = "Created", response = String.class),
                @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
                @ApiResponse(code = 409, message = "Conflicts with another appointment"),
                @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })

    @ApiOperation(value = "Creates an Contact appointment for a specified context")
    public ResponseEntity<AppointmentCreateResponse> createAppointmentWithContextName(final @PathVariable("crn") String crn,
                                                                                      final @PathVariable("sentenceId") Long sentenceId,
                                                                                      final @ApiParam(value = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
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
            @ApiResponse(code = 200, message = "Updated", response = String.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })

    @ApiOperation(value = "Reschedules an appointment")
    public AppointmentRescheduleResponse rescheduleAppointmentWithContextName(final @PathVariable("crn") String crn,
                                                                              final @PathVariable("appointmentId") Long appointmentId,
                                                                              final @ApiParam(value = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                                    @PathVariable("contextName") String context,
                                                                              final @RequestBody ContextlessAppointmentRescheduleRequest appointmentRescheduleRequest) {

        return appointmentService.rescheduleAppointment(crn, appointmentId, context, appointmentRescheduleRequest);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/appointments/{appointmentId}/outcome/context/{contextName}",
        method = RequestMethod.POST,
        consumes = "application/json")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "Updated", response = String.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_INTERVENTIONS_UPDATE"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })

    @ApiOperation(value = "Updates an Contact appointment outcome")
    public AppointmentUpdateResponse updateAppointmentOutcomeWithContext(final @PathVariable("crn") String crn,
                                                                         final @PathVariable("appointmentId") Long appointmentId,
                                                                         final @ApiParam(value = "Name identifying preprocessing applied to the request", example = "commissioned-rehabilitation-services")
                                                                             @PathVariable("contextName") String context,
                                                                         final @RequestBody ContextlessAppointmentOutcomeRequest appointmentOutcomeRequest) {

        return appointmentService.updateAppointmentOutcome(crn, appointmentId, context, appointmentOutcomeRequest);
    }
}
