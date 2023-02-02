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
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.data.api.RiskResourcingDetails;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RiskService;

import java.util.Optional;

@Api(tags = "Risks and Registrations")
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class RiskResource {
    private final RiskService riskService;
    private final OffenderService offenderService;

    @ApiOperation(
        value = "Return the MAPPA details for an offender using CRN", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - requires ROLE_COMMUNITY", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "offenders/crn/{crn}/risk/mappa")
    public MappaDetails getOffenderMappaDetailsByCrn(final @PathVariable("crn") String crn) {
        return mappaDetailsFor(offenderService.offenderIdOfCrn(crn));
    }

    @ApiOperation(
        value = "Return the resourcing details for an offender using NOMS number. Typically this is allocated or retained requiring greater resourcing. This equates to the historic NPS/CRC split for low and high risk offenders", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - requires ROLE_COMMUNITY", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "offenders/nomsNumber/{nomsNumber}/risk/resourcing/latest")
    public RiskResourcingDetails getOffenderResourcingDetailsByNomsNumber(final @PathVariable("nomsNumber") String nomsNumber) {
        final var mayBeOffenderId = offenderService
            .mostLikelyOffenderIdOfNomsNumber(nomsNumber)
            .getOrElseThrow(error -> new ConflictingRequestException(error.getMessage()));

        return resourcingDetailsFor(mayBeOffenderId);
    }

    @ApiOperation(
        value = "Return the resourcing details for an offender using CRN number. Typically this is allocated or retained requiring greater resourcing. This equates to the historic NPS/CRC split for low and high risk offenders", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - requires ROLE_COMMUNITY", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "offenders/crn/{crn}/risk/resourcing/latest")
    public RiskResourcingDetails getOffenderResourcingDetailsByCrn(final @PathVariable("crn") String crn) {
        return resourcingDetailsFor(offenderService.offenderIdOfCrn(crn));
    }


    private MappaDetails mappaDetailsFor(final Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(riskService::getMappaDetails)
            .orElseThrow(() -> new NotFoundException("Offender not found"));
    }
    private RiskResourcingDetails resourcingDetailsFor(final Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(riskService::getResourcingDetails)
            .orElseThrow(() -> new NotFoundException("Offender not found"))
            .orElseThrow(() -> new NotFoundException("Resourcing details for offender not found"));
    }
}
