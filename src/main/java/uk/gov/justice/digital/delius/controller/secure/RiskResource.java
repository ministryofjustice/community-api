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
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.RiskService;

import java.util.Optional;

// TODO - DT-1542 Remove BETA from the API description once the service has been fully implemented
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
        value = "*** BETA - coming soon *** Return the MAPPA details for an offender using offenderId", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - requires ROLE_COMMUNITY", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "offenders/offenderId/{offenderId}/risk/mappa")
    public MappaDetails getOffenderMappaDetailsByOffenderId(final @PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return mappaDetailsFor(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @ApiOperation(
        value = "*** BETA - coming soon *** Return the MAPPA details for an offender using NOMS number", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - requires ROLE_COMMUNITY", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "offenders/nomsNumber/{nomsNumber}/risk/mappa")
    public MappaDetails getOffenderMappaDetailsByNomsNumber(final @PathVariable("nomsNumber") String nomsNumber) {
        return mappaDetailsFor(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @ApiOperation(
        value = "*** BETA - coming soon *** Return the MAPPA details for an offender using CRN", notes = "requires ROLE_COMMUNITY")
    @ApiResponses(
        value = {
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - requires ROLE_COMMUNITY", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Offender not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(value = "offenders/crn/{crn}/risk/mappa")
    public MappaDetails getOffenderMappaDetailsByOffenderId(final @PathVariable("crn") String crn) {
        return mappaDetailsFor(offenderService.offenderIdOfCrn(crn));
    }

    private MappaDetails mappaDetailsFor(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(riskService::getMappaDetails)
            .orElseThrow(() -> new NotFoundException("Offender not found"));
    }
}
