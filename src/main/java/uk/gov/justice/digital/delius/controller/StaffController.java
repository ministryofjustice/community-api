package uk.gov.justice.digital.delius.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.ManagedOffender;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.NoSuchUserException;
import uk.gov.justice.digital.delius.service.StaffService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Api(tags = "Staff")
public class StaffController
{
    private final StaffService staffService;

    @Autowired
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @RequestMapping(value = "/staff/staffCode/{staffCode}/managedOffenders", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A list of managed offenders for a staff officer", response = ManagedOffender.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "The requesting user was restricted from access", response = AccessLimitation.class),
            @ApiResponse(code = 404, message = "The requested staffCode was not found")
    })
    @JwtValidation
    public ResponseEntity<List<ManagedOffender>> getManagedOffendersByStaffCode(final @RequestHeader HttpHeaders httpHeaders,
                                                                                final @PathVariable("staffCode") String staffCode,
                                                                                final @RequestParam(name="current", required=false, defaultValue="false") String current) {
        return staffService.getManagedOffendersByStaffCode(staffCode, current)
                .map(managedOffenders -> new ResponseEntity<>(managedOffenders ,OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
