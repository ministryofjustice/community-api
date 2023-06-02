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
import uk.gov.justice.digital.delius.data.api.OffenderIdentifiers;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

@Tag(name = "Core offender")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class OffenderIdentifiersResource {
    private final OffenderService offenderService;

    @Operation(description = "Return the identifiers for an offender using the crn. requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "Offender not found"),
                    @ApiResponse(responseCode = "500", description = "Unrecoverable error whilst processing request.")
            })
    @GetMapping(value = "offenders/crn/{crn}/identifiers")
    public OffenderIdentifiers getOffenderIdentifiersByCrn(final @PathVariable("crn") String crn) {
        return identifiersFor(offenderService.offenderIdOfCrn(crn));
    }

    private OffenderIdentifiers identifiersFor(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(offenderService::getOffenderIdentifiers)
                .orElseThrow(() -> new NotFoundException("No offender found"));
    }
}
