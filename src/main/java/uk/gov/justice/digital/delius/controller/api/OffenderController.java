package uk.gov.justice.digital.delius.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.Count;
import uk.gov.justice.digital.delius.data.api.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderDetailSummary;
import uk.gov.justice.digital.delius.data.api.OffenderIdsResource;
import uk.gov.justice.digital.delius.data.api.OffenderManager;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@Api(tags = "Offenders resources")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class OffenderController {

    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;
    private final UserService userService;
    private final Jwt jwt;
    private final ObjectMapper objectMapper;

    @Autowired
    public OffenderController(OffenderService offenderService, AlfrescoService alfrescoService, UserService userService, Jwt jwt, ObjectMapper objectMapper) {
        this.offenderService = offenderService;
        this.alfrescoService = alfrescoService;
        this.userService = userService;
        this.jwt = jwt;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetailSummary> getOffenderByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetailSummary> offender = offenderService.getOffenderSummaryByOffenderId(offenderId);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailSummaryNotFound());
    }

    @RequestMapping(value = "/offenders/crn/{crn}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetailSummary> getOffenderSummaryByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("crn") String crn) {
        Optional<OffenderDetailSummary> offender = offenderService.getOffenderSummaryByCrn(crn);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailSummaryNotFound());
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/all", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetail> getFullFatOffenderByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByOffenderId(offenderId);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailNotFound());
    }

    @RequestMapping(value = "/offenders/crn/{crn}/all", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetail> getFullFatOffenderByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("crn") String crn) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByCrn(crn);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailNotFound());

    }

    @RequestMapping(value = "/offenders/offenderIds", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderIdsResource> getOffenderIds(
            final @RequestHeader HttpHeaders httpHeaders,
            @ApiParam(defaultValue = "1000") final @RequestParam(name = "pageSize", required = false, defaultValue = "${offender.ids.pagesize:1000}") int pageSize,
            final @RequestParam(defaultValue = "1") int page) {

        final var offenderIds = offenderService.allOffenderIds(pageSize, page);
        if (offenderIds.isEmpty()) {
            return new ResponseEntity<>(NOT_FOUND);
        }

        return new ResponseEntity<>(OffenderIdsResource.builder().offenderIds(offenderIds).build(), OK);
    }

    @RequestMapping(value = "/offenders/count", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<Count> offenderCount(final @RequestHeader HttpHeaders httpHeaders) {
        return new ResponseEntity<>(Count.builder().value(offenderService.getOffenderCount()).build(), OK);
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/userAccess", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User has unrestricted access to offender"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "User is restricted from access to offender", response = AccessLimitation.class),
            @ApiResponse(code = 404, message = "No such offender, or no such User (see body for detail)")
    })
    @JwtValidation
    public ResponseEntity<AccessLimitation> checkUserAccessByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                        final @PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);

        return accessLimitationResponseEntityOf(httpHeaders, maybeOffender);
    }

    private ResponseEntity<AccessLimitation> accessLimitationResponseEntityOf(@RequestHeader HttpHeaders httpHeaders, Optional<OffenderDetail> maybeOffender) {
        if (maybeOffender.isEmpty()) {
            return new ResponseEntity<>(NOT_FOUND);
        }

        OffenderDetail offenderDetail = maybeOffender.get();

        Claims claims = jwt.parseAuthorizationHeader(httpHeaders.getFirst(HttpHeaders.AUTHORIZATION)).get();

        AccessLimitation accessLimitation = userService.accessLimitationOf((String) claims.get(Jwt.UID), offenderDetail);

        return new ResponseEntity<>(accessLimitation, responseCodeOf(accessLimitation));
    }

    private HttpStatus responseCodeOf(AccessLimitation accessLimitation) {
        if (accessLimitation.isUserExcluded() || accessLimitation.isUserRestricted()) {
            return FORBIDDEN;
        }
        return OK;
    }

    private ResponseEntity<OffenderDetail> offenderDetailNotFound() {
        return new ResponseEntity<>(OffenderDetail.builder().build(), NOT_FOUND);
    }

    private ResponseEntity<OffenderDetailSummary> offenderDetailSummaryNotFound() {
        return new ResponseEntity<>(OffenderDetailSummary.builder().build(), NOT_FOUND);
    }
}
