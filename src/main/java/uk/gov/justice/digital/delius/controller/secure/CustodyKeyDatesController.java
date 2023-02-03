package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CreateCustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.ConvictionService.CustodyTypeCodeIsNotValidException;
import uk.gov.justice.digital.delius.service.ConvictionService.DuplicateActiveCustodialConvictionsException;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RestController
@Slf4j
@Api(tags = {"Sentence dates", "Custody"}, authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
@AllArgsConstructor
public class CustodyKeyDatesController {
    private final OffenderService offenderService;
    private final ConvictionService convictionService;
    private final FeatureSwitches featureSwitches;

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates/{typeCode}", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be added to an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Adds or replaces a custody key date for the active custodial conviction", notes = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public CustodyKeyDate putCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber,
                                                        final @PathVariable String typeCode,
                                                        final @RequestBody CreateCustodyKeyDate custodyKeyDate) {
        return addOrReplaceCustodyKeyDate(offenderService.mostLikelyOffenderIdOfNomsNumber(nomsNumber).getOrElseThrow(e -> new ConflictingRequestException(e.getMessage())), typeCode, custodyKeyDate);
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", method = RequestMethod.POST, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 404, message = "The requested offender or conviction was not found.")
    })
    @ApiOperation(value = "Replaces all key dates specified in body. Key dates are either added or replaced or deleted if absent (see ReplaceCustodyKeyDates for the list). The the custodial conviction must be active", notes = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public Custody replaceAllCustodyKeyDateByNomsNumberAndBookingNumber(final @PathVariable String nomsNumber,
                                                                        final @PathVariable String bookingNumber,
                                                                        final @RequestBody ReplaceCustodyKeyDates replaceCustodyKeyDates) {
        final var offenderId = offenderService.mostLikelyOffenderIdOfNomsNumber(nomsNumber)
            .getOrElseThrow((e) -> e)
            .orElseThrow(() -> new NotFoundException(String.format("Offender with NOMS number %s not found", nomsNumber)));
        final var activeCustodialEvents = convictionService.getAllActiveCustodialEventsWithBookingNumber(offenderId, bookingNumber);
        if (activeCustodialEvents.isEmpty()) {
            throw new NotFoundException(String.format("Conviction with bookingNumber %s not found for offender with NOMS number %s", bookingNumber, nomsNumber));
        }

        // legacy behaviour - do not update multiple events
        if (activeCustodialEvents.size() > 1 && !featureSwitches.getNoms().getUpdate().getMultipleEvents().isUpdateBulkKeyDates()) {
            log.warn("Multiple active custodial convictions found for {} for offender {}", bookingNumber, nomsNumber);
            throw new NotFoundException(String.format("Single active conviction for %s with booking number %s not found. Instead has %d convictions", nomsNumber, bookingNumber, activeCustodialEvents.size()));
        }

        return activeCustodialEvents
            .stream()
            .map(event -> convictionService.addOrReplaceOrDeleteCustodyKeyDates(offenderId, event.getEventId(), replaceCustodyKeyDates))
            .max(Comparator.comparing(Custody::getSentenceStartDate))
            .orElseThrow();
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates/{typeCode}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be retrieved for an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found or does not have the supplied key date type.")
    })
    @ApiOperation(value = "Gets a custody key date for the active custodial conviction")
    public CustodyKeyDate getCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber,
                                                        final @PathVariable String typeCode) {
        return getCustodyKeyDate(offenderService.mostLikelyOffenderIdOfNomsNumber(nomsNumber).getOrElseThrow(e -> new ConflictingRequestException(e.getMessage())), typeCode);
    }


    @RequestMapping(value = "offenders/crn/{crn}/custody/keyDates", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The the offender does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Gets a all custody key dates for the active custodial conviction")
    public List<CustodyKeyDate> getAllCustodyKeyDateByCrn(final @PathVariable String crn) {
        return getCustodyKeyDates(offenderService.offenderIdOfCrn(crn));
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The the offender does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Gets all custody key dates for the active custodial conviction")
    public List<CustodyKeyDate> getAllCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber) {
        return getCustodyKeyDates(offenderService.mostLikelyOffenderIdOfNomsNumber(nomsNumber).getOrElseThrow(e -> new ConflictingRequestException(e.getMessage())));
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates/{typeCode}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Key date has been deleted"),
            @ApiResponse(code = 403, message = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be deleted from an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Deletes the custody key date for the active custodial conviction", notes = "Requires role ROLE_COMMUNITY_CUSTODY_UPDATE")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public void deleteCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber,
                                                 final @PathVariable String typeCode) {
        deleteCustodyKeyDate(offenderService.mostLikelyOffenderIdOfNomsNumber(nomsNumber).getOrElseThrow(e -> new ConflictingRequestException(e.getMessage())), typeCode);
    }

    private CustodyKeyDate addOrReplaceCustodyKeyDate(final Optional<Long> maybeOffenderId, final String typeCode, final CreateCustodyKeyDate custodyKeyDate) {
        return maybeOffenderId
                .map(offenderId -> {
                    try {
                        return convictionService.addOrReplaceCustodyKeyDateByOffenderId(offenderId, typeCode, custodyKeyDate);
                    } catch (final CustodyTypeCodeIsNotValidException e) {
                        log.warn("Key type code is not valid for {}", typeCode);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found"));
    }

    private CustodyKeyDate getCustodyKeyDate(final Optional<Long> maybeOffenderId, final String typeCode) {
        return maybeOffenderId
                .map(getCustodyKeyDateByOffenderId(typeCode))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found"));
    }

    private Function<Long, CustodyKeyDate> getCustodyKeyDateByOffenderId(final String typeCode) {
        return offenderId -> convictionService.getCustodyKeyDateByOffenderId(offenderId, typeCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Key date not found"));
    }

    private List<CustodyKeyDate> getCustodyKeyDates(final Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(convictionService::getCustodyKeyDatesByOffenderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found"));
    }

    private void deleteCustodyKeyDate(final Optional<Long> maybeOffenderId, final String typeCode) {
        if (maybeOffenderId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found");
        }
        maybeOffenderId.ifPresent(offenderId -> convictionService.deleteCustodyKeyDateByOffenderId(offenderId, typeCode));
    }

}
