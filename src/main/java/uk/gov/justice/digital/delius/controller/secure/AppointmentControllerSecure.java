package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Appointment.Attended;
import uk.gov.justice.digital.delius.data.api.AppointmentDetail;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.service.AppointmentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@Api(tags = {"Appointments"})
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AppointmentControllerSecure {
    private final AppointmentService appointmentService;
    private final OffenderService offenderService;

    @RequestMapping(value = "/offenders/crn/{crn}/appointments", method = RequestMethod.GET)
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "All offender appointments", response = AppointmentDetail.class, responseContainer = "List"),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @ApiOperation(value = "Gets all appointments for a specific offender by CRN")
    public List<AppointmentDetail> getOffenderAppointmentsByCrn(
        final @PathVariable("crn") String crn,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("from") @ApiParam(value = "date of the earliest appointment") Optional<LocalDate> from,
        final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("to") @ApiParam(value = "date of the latest appointment") Optional<LocalDate> to,
        final @RequestParam("attended") Optional<Attended> attended) {

        final var filter = AppointmentFilter.builder()
            .from(from).to(to)
            .attended(attended)
            .build();
        final var id = offenderService.offenderIdOfCrn(crn)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s does not exist", crn)));
        return appointmentService.appointmentDetailsFor(id, filter);
    }
}
