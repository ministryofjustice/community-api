package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationAreaWithLocalDeliveryUnits;
import uk.gov.justice.digital.delius.data.api.ReferenceDataList;
import uk.gov.justice.digital.delius.data.api.ReferenceDataSets;
import uk.gov.justice.digital.delius.service.ReferenceDataService;

import java.util.List;

@Slf4j
@Api(tags = "Reference data", authorizations = {@Authorization("ROLE_COMMUNITY")})
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
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping("/probationAreas")
    public Page<KeyValue> getProbationAreaCodes(
            @ApiParam(name = "active", value = "Restricts to active areas only", example = "true") final @RequestParam(name = "active", required = false) boolean restrictActive,
            @ApiParam(name = "excludeEstablishments", value = "Restricts to areas that are providers, no prisons will be returned", example = "true") final @RequestParam(name = "excludeEstablishments", required = false) boolean excludeEstablishments) {

        log.info("Call to getProbationAreaCodes");
        return referenceDataService.getProbationAreasCodes(restrictActive, excludeEstablishments);
    }

    @ApiOperation(
            value = "Return Probation Areas and associated Local Delivery Units. Establishments are excluded.",
            notes = "Accepts filtering to only return active areas")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping("/probationAreas/localDeliveryUnits")
    public List<ProbationAreaWithLocalDeliveryUnits> getProbationAreasAndLocalDeliveryUnits(
            @ApiParam(name = "active", value = "Restricts to active areas only", example = "true") final @RequestParam(name = "active", required = false) boolean restrictActive) {
        return referenceDataService.getProbationAreasAndLocalDeliveryUnits(restrictActive);
    }

    @ApiOperation(
            value = "Return Local delivery units for a probation area",
            notes = "Accepts a probation area code")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/probationAreas/code/{code}/localDeliveryUnits")
    public Page<KeyValue> getLdusForProbationCode(
            @ApiParam(name = "code", value = "Probation area code", example = "NO2", required = true) final @PathVariable String code) {
        return referenceDataService.getLocalDeliveryUnitsForProbationArea(code);
    }

    @ApiOperation(
            value = "Return teams for a local delivery unit within a probation area",
            notes = "Accepts a probation area code and local delivery unit code")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "Not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/probationAreas/code/{code}/localDeliveryUnits/code/{lduCode}/teams")
    public Page<KeyValue> getTeamsForLdu(
            @ApiParam(name = "code", value = "Probation area code", example = "NO2", required = true) final @PathVariable String code,
            @ApiParam(name = "lduCode", value = "Local delivery unit code", example = "NO2NPSA", required = true) final @PathVariable String lduCode) {

        log.info("Call to getTeamsForLdu");
        return referenceDataService.getTeamsForLocalDeliveryUnit(code, lduCode);
    }
}
