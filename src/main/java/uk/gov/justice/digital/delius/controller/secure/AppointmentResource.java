package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.service.AppointmentService;

@RestController
@Slf4j
@Api(tags = {"Contact and attendance"})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@AllArgsConstructor
public class AppointmentResource {
    private final AppointmentService appointmentService;

    @RequestMapping(value = "/offenders/crn/{crn}/conviction/{conviction}/appointments", method = RequestMethod.POST, consumes = "application/json")
    public void createAppointment(final @PathVariable("crn") String crn,
                                  final @PathVariable("conviction") Long conviction,
                                  final @RequestBody AppointmentCreateRequest appointmentCreateRequest) {
        appointmentService.createAppointment(crn, conviction, appointmentCreateRequest);
    }
}
