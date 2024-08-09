package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Attendances;
import uk.gov.justice.digital.delius.service.AttendanceService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Contact and attendance", description = "Requires ROLE_COMMUNITY")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_COMMUNITY','ROLE_PROBATION_INTEGRATION_ADMIN')")
public class AttendanceResource {

    public static final String MSG_OFFENDER_NOT_FOUND = "Offender ID not found for CRN %s";

    private final AttendanceService attendanceService;
    private final OffenderService offenderService;

    @Autowired
    public AttendanceResource (final AttendanceService attendanceService, final OffenderService offenderService) {
        this.offenderService = offenderService;
        this.attendanceService = attendanceService;
    }

    @GetMapping(value = "/offenders/crn/{crn}/convictions/{convictionId}/attendancesFilter", produces = APPLICATION_JSON_VALUE)
    @Operation(description = "Return the attendances for a CRN and a conviction id, filtered.")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    public Attendances getAttendancesByConviction(final @PathVariable("crn") String crn,
                                                final @PathVariable("convictionId") Long convictionId) {
        final Long offenderId = getOffenderId(crn);
        return new Attendances(AttendanceService.attendancesFor(attendanceService.getContactsForEvent(offenderId, convictionId, LocalDate.now())));
    }

    private Long getOffenderId(String crn) {
        return offenderService.offenderIdOfCrn(crn).orElseThrow(() -> new NotFoundException(String.format(MSG_OFFENDER_NOT_FOUND, crn)));
    }

}

