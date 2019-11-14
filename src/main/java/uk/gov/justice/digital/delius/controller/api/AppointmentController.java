package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.Appointment;
import uk.gov.justice.digital.delius.data.api.Appointment.Attended;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.filters.AppointmentFilter;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.AppointmentService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@Api(description = "Offender appointment resources", tags = "Offender Appointments")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppointmentController {

    private final OffenderService offenderService;
    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(OffenderService offenderService, AppointmentService appointmentService) {
        this.offenderService = offenderService;
        this.appointmentService = appointmentService;
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/appointments", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Appointment>> getOffenderAppointmentReportByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                      final @PathVariable("offenderId") Long offenderId,
                                                                                      final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("from") @ApiParam(value = "date of the earliest appointment") Optional<LocalDate> from,
                                                                                      final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("to") @ApiParam(value = "date of the latest appointment") Optional<LocalDate> to,
                                                                                      final @RequestParam("attended") Optional<Attended> attended) {

        AppointmentFilter appointmentFilter = AppointmentFilter.builder()
                .from(from)
                .to(to)
                .attended(attended)
                .build();

        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return appointmentsResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId), appointmentFilter);
    }

    private ResponseEntity<List<Appointment>> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/appointments", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Appointment>> getOffenderReportAppointmentByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                               final @PathVariable("crn") String crn,
                                                                               final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("from") @ApiParam(value = "date of the earliest appointment") Optional<LocalDate> from,
                                                                               final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("to") @ApiParam(value = "date of the latest appointment") Optional<LocalDate> to,
                                                                               final @RequestParam("attended") Optional<Attended> attended) {

        AppointmentFilter appointmentFilter = AppointmentFilter.builder()
                .from(from)
                .to(to)
                .attended(attended)
                .build();

        return appointmentsResponseEntityOf(offenderService.offenderIdOfCrn(crn), appointmentFilter);

    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/appointments", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Appointment>> getOffenderAppointmentReportByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                                      final @PathVariable("nomsNumber") String nomsNumber,
                                                                                      final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("from") @ApiParam(value = "date of the earliest appointment") Optional<LocalDate> from,
                                                                                      final @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam("to") @ApiParam(value = "date of the latest appointment") Optional<LocalDate> to,
                                                                                      final @RequestParam("attended") Optional<Attended> attended) {

        AppointmentFilter appointmentFilter = AppointmentFilter.builder()
                .from(from)
                .to(to)
                .attended(attended)
                .build();

        return appointmentsResponseEntityOf(offenderService.offenderIdOfNomsNumber(nomsNumber), appointmentFilter);

    }

    private ResponseEntity<List<Appointment>> appointmentsResponseEntityOf(Optional<Long> maybeOffenderId, AppointmentFilter filter) {
        return maybeOffenderId
                .map(offenderId -> new ResponseEntity<>(appointmentService.appointmentsFor(offenderId, filter), HttpStatus.OK))
                .orElseGet(this::notFound);
    }

}