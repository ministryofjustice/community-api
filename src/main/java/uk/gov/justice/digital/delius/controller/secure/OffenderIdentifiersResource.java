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
import uk.gov.justice.digital.delius.data.api.OffenderIdentifiers;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

@Api(tags = "Offender identifiers resource (Secure)")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class OffenderIdentifiersResource {
    private final OffenderService offenderService;

    @ApiOperation(
            value = "Return the identifiers for an offender using offenderId", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "offenders/offenderId/{offenderId}/identifiers")
    public OffenderIdentifiers getOffenderIdentifiersByOffenderId(final @PathVariable("offenderId") Long offenderId) {
        final var maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return identifiersFor(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @ApiOperation(
            value = "Return the identifiers for an offender using NOMS number", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "offenders/nomsNumber/{nomsNumber}/identifiers")
    public OffenderIdentifiers getOffenderIdentifiersByNomsNumber(final @PathVariable("nomsNumber") String nomsNumber) {
        return identifiersFor(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @ApiOperation(
            value = "Return the identifiers for an offender using the crn", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
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
