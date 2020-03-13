package uk.gov.justice.digital.delius.controller.secure;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Attendance;
import uk.gov.justice.digital.delius.data.api.Attendances;
import uk.gov.justice.digital.delius.service.AttendanceService;

@Api(tags = "Attendance resources (Secure)", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class AttendanceResource {

    public static final String MSG_ATTENDANCES_NOT_FOUND = "Attendances with event ID %s not found";
    private final AttendanceService attendanceService;

    public AttendanceResource (@Autowired final AttendanceService service) {
        this.attendanceService = service;
    }

    @GetMapping(value = "/contacts/{eventId}/attendances", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return the attendances for an event id where enforcement is flagged")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = Attendance.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    public ResponseEntity<Attendances> getAttendancesForEventId(final @RequestHeader HttpHeaders httpHeaders,
        final @PathVariable("eventId") Long eventId) {

        log.info("Call to getAttendancesForEventId {}", eventId);
        final LocalDate localDate = LocalDate.now();

        final List<Attendance> attendances = attendanceService.getContactsForEvent(eventId, localDate)
            .map(AttendanceService::attendancesFor)
            .orElseThrow(() -> new NotFoundException(String.format(MSG_ATTENDANCES_NOT_FOUND, eventId)));

        return new ResponseEntity<>(new Attendances(attendances), OK);
    }

}

