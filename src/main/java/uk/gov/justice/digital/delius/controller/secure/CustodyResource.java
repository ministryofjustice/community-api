package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@Slf4j
@Tag(name = "Custody", description = "Requires ROLE_COMMUNITY_CUSTODY_UPDATE")
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
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(responseCode = "400", description = "The custody request is invalid"),
            @ApiResponse(responseCode = "404", description = "Either the requested offender was not found or the conviction associated the booking number.")
    })
    @Operation(description = "Updates the associated custody record with changes defined in UpdateCustody")
    public Custody updateCustody(final @PathVariable String nomsNumber,
                                 final @PathVariable String bookingNumber,
                                 final @RequestBody @Valid UpdateCustody updateCustody) {
        return custodyService.updateCustodyPrisonLocation(nomsNumber, bookingNumber, updateCustody);
    }


    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/bookingNumber", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(responseCode = "400", description = "The booking number custody request is invalid"),
            @ApiResponse(responseCode = "404", description = "Either the requested offender was not found or the conviction associated the sentence start date")
    })
    @Operation(description = "Updates the associated custody record with booking number in UpdateCustodyBookingNumber")
    public Custody updateCustodyBookingNumber(final @PathVariable String nomsNumber,
                                              final @RequestBody @Valid UpdateCustodyBookingNumber updateCustodyBookingNumber) {
        return custodyService.updateCustodyBookingNumber(nomsNumber, updateCustodyBookingNumber);
    }

    @RequestMapping(value = "offenders/crn/{crn}/nomsNumber", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(responseCode = "404", description = "The requested offender was not found")
    })
    @Operation(description = "Updates the offender record with the NOMS number in UpdateOffenderNomsNumber")
    public IDs updateOffenderNomsNumber(final @PathVariable String crn,
                                              final @RequestBody @Valid UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        return offenderIdentifierService.updateNomsNumber(crn, updateOffenderNomsNumber);
    }

    @RequestMapping(value = "offenders/nomsNumber/{originalNomsNumber}/nomsNumber", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "The new noms number is not present in request"),
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(responseCode = "404", description = "The requested offender was not found"),
            @ApiResponse(responseCode = "409", description = "The new noms number is assigned to an existing offender already")
    })
    @Operation(description = "Updates the offender record(s) with the new NOMS number in UpdateOffenderNomsNumber replacing the existing number. In the very rare circumstances more than one offender is found with matching noms number, all will be updated and their identifiers returned.")
    public List<IDs> replaceOffenderNomsNumber(final @PathVariable String originalNomsNumber,
                                               final @RequestBody @Valid UpdateOffenderNomsNumber updateOffenderNomsNumber) {
        return offenderIdentifierService.replaceNomsNumber(originalNomsNumber, updateOffenderNomsNumber);
    }


    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/bookingNumber/{bookingNumber}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Requires role ROLE_COMMUNITY"),
            @ApiResponse(responseCode = "404", description = "Either the requested offender was not found or the conviction associated the booking number.")
    })
    @Operation(description = "Gets the current custody record")
    @PreAuthorize("hasRole('ROLE_COMMUNITY')")
    public Custody getCustodyByBookNumber(final @PathVariable String nomsNumber,
                                 final @PathVariable String bookingNumber) {
        return custodyService.getCustodyByBookNumber(nomsNumber, bookingNumber);
    }
}
