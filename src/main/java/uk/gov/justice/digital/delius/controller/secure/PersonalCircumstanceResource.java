package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstances;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.PersonalCircumstanceService;

import java.util.Optional;

@Tag(name = "Personal circumstances")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class PersonalCircumstanceResource {
    private final OffenderService offenderService;
    private final PersonalCircumstanceService personalCircumstanceService;

    @GetMapping(value = "offenders/crn/{crn}/personalCircumstances")
    @Operation(description = "Return the personal circumstances for an offender using crn. requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "Offender not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    public PersonalCircumstances getOffenderPersonalCircumstancesByCrn(final @PathVariable("crn") String crn) {
        return personalCircumstancesOf(offenderService.offenderIdOfCrn(crn));
    }

    private PersonalCircumstances personalCircumstancesOf(final Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(offenderId -> new PersonalCircumstances(personalCircumstanceService
                        .personalCircumstancesFor(offenderId)))
                .orElseThrow(() -> new NotFoundException("No offender found"));
    }

}
