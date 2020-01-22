package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.UpdateCustody;
import uk.gov.justice.digital.delius.service.CustodyService;

import javax.validation.Valid;

@RestController
@Slf4j
@Api(tags = {"Custody resource"}, authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class CustodyResource {
    private final CustodyService custodyService;

    public CustodyResource(CustodyService custodyService) {
        this.custodyService = custodyService;
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


}
