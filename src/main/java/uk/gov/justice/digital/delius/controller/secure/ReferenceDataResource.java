package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.ReferenceDataService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Api(tags = "Reference Data API (Secure)", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class ReferenceDataResource {

    private final ReferenceDataService referenceDataService;

    @ApiOperation(
            value = "Return probation areas",
            notes = "Accepts filtering to only return active areas")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping("/probationAreas")
    public Page<KeyValue> getProbationAreaCodes(
            @ApiParam(name = "active", value = "Restricts to active areas only", example = "true", required = false)
            final @RequestParam(name = "active", required = false) boolean restrictActive) {

        log.info("Call to getProbationAreaCodes");
        return referenceDataService.getProbationAreasCodes(restrictActive);
    }

    @ApiOperation(
            value = "Return Local delivery units for a probation area",
            notes = "Accepts a probation area code")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/probationAreas/code/{code}/localDeliveryUnits")
    public Page<KeyValue> getLdusForProbationCode(
            @ApiParam(name = "code", value = "Probation area code", example = "NO2", required = true)
            final @PathVariable String code) {

        log.info("Call to getLdusForProbationCode");
        return referenceDataService.getLocalDeliveryUnitsForProbationArea(code);
    }

    @ApiOperation(
            value = "Return teams for a local delivery unit within a probation area",
            notes = "Accepts a probation area code and local delivery unit code")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/probationAreas/code/{code}/localDeliveryUnits/code/{lduCode}/teams")
    public Page<KeyValue> getTeamsForLdu(
            @ApiParam(name = "code", value = "Probation area code", example = "NO2", required = true)
            final @PathVariable String code,
            @ApiParam(name = "lduCode", value = "Local delivery unit code", example = "NO2NPSA", required = true)
            final @PathVariable String lduCode) {

        log.info("Call to getTeamsForLdu");
        return referenceDataService.getTeamsForLocalDeliveryUnit(code, lduCode);
    }
}
