package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.Registrations;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RegistrationService;

import java.util.Optional;

@Api(tags = "Risks and Registrations")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class RegistrationResource {
    private final OffenderService offenderService;
    private final RegistrationService registrationService;

    @ApiOperation(
            value = "Return the registrations for an offender using offenderId", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "offenders/offenderId/{offenderId}/registrations")
    public Registrations getOffenderRegistrationsByOffenderId(final @PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return registrationsOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @ApiOperation(
            value = "Return the registrations for an offender using NOMS number", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "offenders/nomsNumber/{nomsNumber}/registrations")
    public Registrations getOffenderRegistrationsByNomsNumber(final @PathVariable("nomsNumber") String nomsNumber) {
        return registrationsOf(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @ApiOperation(
            value = "Return the registrations for an offender using the crn", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "offenders/crn/{crn}/registrations")
    public Registrations getOffenderRegistrationsByCrn(final @PathVariable("crn") String crn) {
        return registrationsOf(offenderService.offenderIdOfCrn(crn));
    }

    private Registrations registrationsOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(offenderId -> new Registrations(registrationService.registrationsFor(offenderId)))
                .orElseThrow(() -> new NotFoundException("No offender found"));
    }
}
