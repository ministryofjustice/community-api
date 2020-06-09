package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.IDs;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.data.api.UpdateCustodyBookingNumber;
import uk.gov.justice.digital.delius.data.api.UpdateOffenderNomsNumber;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.OffenderIdentifierService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@Api(tags = {"Custody resource"}, authorizations = {@Authorization("ROLE_COMMUNITY_CUSTODY_UPDATE")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
public class CustodyResource {
    private final CustodyService custodyService;
    private final OffenderIdentifierService offenderIdentifierService;

    public CustodyResource(CustodyService custodyService, OffenderIdentifierService offenderIdentifierService) {
        this.custodyService = custodyService;
        this.offenderIdentifierService = offenderIdentifierService;
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/bookingNumber/{bookingNumber}", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody record was updated", response = Custody.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The custody request is invalid"),
            @ApiResponse(code = 404, message = "Either the requested offender was not found or the conviction associated the booking number.")
    })
    @ApiOperation(value = "Updates the associated custody record with changes defined in UpdateCustody")
    public Custody updateCustody(final @PathVariable String nomsNumber,
                                 final @PathVariable String bookingNumber,
                                 final @RequestBody @Valid UpdateCustody updateCustody) {
        log.info("Call to updateCustody for {} booking {}", nomsNumber, bookingNumber);

        return custodyService.updateCustody(nomsNumber, bookingNumber, updateCustody);
    }


    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/bookingNumber", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody record was updated", response = Custody.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The booking number custody request is invalid"),
            @ApiResponse(code = 404, message = "Either the requested offender was not found or the conviction associated the sentence start date")
    })
    @ApiOperation(value = "Updates the associated custody record with booking number in UpdateCustodyBookingNumber")
    public Custody updateCustodyBookingNumber(final @PathVariable String nomsNumber,
                                              final @RequestBody @Valid UpdateCustodyBookingNumber updateCustodyBookingNumber) {
        log.info("Call to updateCustodyBookingNumber for {}", nomsNumber);

        return custodyService.updateCustodyBookingNumber(nomsNumber, updateCustodyBookingNumber);
    }

    @RequestMapping(value = "offenders/crn/{crn}/nomsNumber", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The offender record was updated", response = IDs.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 404, message = "The requested offender was not found")
    })
    @ApiOperation(value = "Updates the offender record with the NOMS number in UpdateOffenderNomsNumber")
    public IDs updateOffenderNomsNumber(final @PathVariable String crn,
                                              final @RequestBody @Valid UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        log.info("Call to updateOffenderNomsNumber for {}", crn);

        return offenderIdentifierService.updateNomsNumber(crn, updateOffenderNomsNumber);
    }

    @RequestMapping(value = "offenders/nomsNumber/{originalNomsNumber}/nomsNumber", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The new noms number is not present"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "Missing required role for this operation"),
            @ApiResponse(code = 404, message = "The requested offender was not found")
    })
    @ApiOperation(value = "Updates the offender record(s) with the new NOMS number in UpdateOffenderNomsNumber replacing the existing number.",
            notes = "In the very rare circumstances more than one offender is found with matching noms number, all will be updated and their identifiers returned.")
    public List<IDs> replaceOffenderNomsNumber(final @PathVariable String originalNomsNumber,
                                               final @RequestBody @Valid UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        log.info("Call to replaceOffenderNomsNumber for {}", originalNomsNumber);

        return offenderIdentifierService.replaceNomsNumber(originalNomsNumber, updateOffenderNomsNumber);
    }
}
