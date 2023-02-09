package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.Registrations;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RegistrationService;
import uk.gov.justice.digital.delius.service.UserAccessService;

import java.util.Optional;

@Tag(name = "Risks and Registrations")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class RegistrationResource {
    private final OffenderService offenderService;
    private final RegistrationService registrationService;
    private final UserAccessService userAccessService;

    @Operation(description = "Return the registrations for an offender using NOMS number. requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Multiple offenders found in the same state ")
        })
    @GetMapping(value = "offenders/nomsNumber/{nomsNumber}/registrations")
    public Registrations getOffenderRegistrationsByNomsNumber(final @PathVariable("nomsNumber") String nomsNumber,
                                                              final @RequestParam(value = "failOnDuplicate", defaultValue = "false") boolean failOnDuplicate) {
        final Optional<Long> mayBeOffenderId;
        if (failOnDuplicate) {
            mayBeOffenderId = offenderService
                .singleOffenderIdOfNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));

        } else {
            mayBeOffenderId = offenderService
                .mostLikelyOffenderIdOfNomsNumber(nomsNumber)
                .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));
        }

        return registrationsOf(mayBeOffenderId);
    }

    @Operation(description = "Return the registrations for an offender using the crn. requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Offender not found"),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
        })
    @GetMapping(value = "offenders/crn/{crn}/registrations")
    public Registrations getOffenderRegistrationsByCrn(
        final @PathVariable("crn") String crn,
        @Parameter(name = "activeOnly", description = "retrieve only active registrations", example = "true")
        @RequestParam(name = "activeOnly", required = false, defaultValue = "false") final boolean activeOnly,
        Authentication authentication
    ) {
        userAccessService.checkExclusionsAndRestrictions(crn, authentication.getAuthorities());
        if (activeOnly) {
            return activeRegistrationsOf(offenderService.offenderIdOfCrn(crn));
        }
        return registrationsOf(offenderService.offenderIdOfCrn(crn));
    }

    private Registrations activeRegistrationsOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new Registrations(registrationService.activeRegistrationsFor(offenderId)))
            .orElseThrow(() -> new NotFoundException("No offender found"));
    }

    private Registrations registrationsOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new Registrations(registrationService.registrationsFor(offenderId)))
            .orElseThrow(() -> new NotFoundException("No offender found"));
    }
}
