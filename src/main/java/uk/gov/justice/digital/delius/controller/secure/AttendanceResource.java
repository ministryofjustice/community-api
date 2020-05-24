package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = "Attendance resources (Secure)", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class AttendanceResource {

    public static final String MSG_OFFENDER_NOT_FOUND = "Offender ID not found for CRN %s";

    private final AttendanceService attendanceService;
    private final OffenderService offenderService;

    @Autowired
    public AttendanceResource (final AttendanceService attendanceService, final OffenderService offenderService) {
        this.offenderService = offenderService;
        this.attendanceService = attendanceService;
    }

    @GetMapping(value = "/offenders/crn/{crn}/convictions/{convictionId}/attendances", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Return the attendances for a CRN and a conviction id where enforcement is flagged")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = Attendance.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    public Attendances getAttendances(final @RequestHeader HttpHeaders httpHeaders,
                                    final @PathVariable("crn") String crn,
                                    final @PathVariable("convictionId") Long convictionId) {

        log.info("Call to getAttendances for CRN {} and conviction ID {}", crn, convictionId);
        final LocalDate localDate = LocalDate.now();
        final Long offenderId = offenderService.offenderIdOfCrn(crn)
            .orElseThrow(() -> new NotFoundException(String.format(MSG_OFFENDER_NOT_FOUND, crn)));

        return new Attendances(AttendanceService.attendancesFor(attendanceService.getContactsForEvent(offenderId, convictionId, localDate)));
    }

}

