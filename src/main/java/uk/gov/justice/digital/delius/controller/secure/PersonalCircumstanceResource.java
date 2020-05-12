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
import uk.gov.justice.digital.delius.data.api.PersonalCircumstancesWrapper;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.PersonalCircumstanceService;

import java.util.Optional;

@Api(tags = "Offender personal circumstance resource (Secure)")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class PersonalCircumstanceResource {
    private final OffenderService offenderService;
    private final PersonalCircumstanceService personalCircumstanceService;

    @GetMapping(value = "offenders/offenderId/{offenderId}/personalCircumstances")
    @ApiOperation(
            value = "Return the personal circumstances for an offender using offenderId")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    public PersonalCircumstancesWrapper getOffenderPersonalCircumstancesByOffenderId(final @PathVariable("offenderId") Long offenderId) {
        final var maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return personalCircumstancesResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @GetMapping(value = "offenders/nomsNumber/{nomsNumber}/personalCircumstances")
    @ApiOperation(
            value = "Return the personal circumstances for an offender using noms number")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    public PersonalCircumstancesWrapper getOffenderPersonalCircumstancesByNomsNumber(final @PathVariable("nomsNumber") String nomsNumber) {
        return personalCircumstancesResponseEntityOf(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @GetMapping(value = "offenders/crn/{crn}/personalCircumstances")
    @ApiOperation(
            value = "Return the personal circumstances for an offender using crn")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    public PersonalCircumstancesWrapper getOffenderPersonalCircumstancesByCrn(final @PathVariable("crn") String crn) {
        return personalCircumstancesResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    private PersonalCircumstancesWrapper personalCircumstancesResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(offenderId -> new PersonalCircumstancesWrapper(personalCircumstanceService
                        .personalCircumstancesFor(offenderId)))
                .orElseThrow(() -> new NotFoundException("No offender found"));
    }

}
