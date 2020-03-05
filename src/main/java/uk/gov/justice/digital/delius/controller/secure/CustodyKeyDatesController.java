package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.ConvictionService.CustodyTypeCodeIsNotValidException;
import uk.gov.justice.digital.delius.service.ConvictionService.DuplicateConvictionsForBookingNumberException;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RestController
@Slf4j
@Api(tags = {"Offender custody key dates", "OMiC"}, authorizations = {@Authorization("ROLE_COMMUNITY")})
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class CustodyKeyDatesController {
    private final OffenderService offenderService;
    private final ConvictionService convictionService;

    @Autowired
    public CustodyKeyDatesController(OffenderService offenderService, ConvictionService convictionService) {
        this.offenderService = offenderService;
        this.convictionService = convictionService;
    }

    @RequestMapping(value = "offenders/crn/{crn}/custody/keyDates/{typeCode}", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The new or updated custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be added to an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Adds or replaces a custody key date for the active custodial conviction")
    public CustodyKeyDate putCustodyKeyDateByCrn(final @PathVariable String crn,
                                                 final @PathVariable String typeCode,
                                                 final @RequestBody CreateCustodyKeyDate custodyKeyDate) {
        log.info("Call to putCustodyKeyDateByCrn for {} code {}", crn, typeCode);
        return addOrReplaceCustodyKeyDate(offenderService.offenderIdOfCrn(crn), typeCode, custodyKeyDate);
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates/{typeCode}", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The new or updated custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be added to an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Adds or replaces a custody key date for the active custodial conviction")
    public CustodyKeyDate putCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber,
                                                        final @PathVariable String typeCode,
                                                        final @RequestBody CreateCustodyKeyDate custodyKeyDate) {
        log.info("Call to putCustodyKeyDateByNomsNumber for {} code {}", nomsNumber, typeCode);
        return addOrReplaceCustodyKeyDate(offenderService.offenderIdOfNomsNumber(nomsNumber), typeCode, custodyKeyDate);
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/bookingNumber/{bookingNumber}/custody/keyDates", method = RequestMethod.POST, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The new or updated custody key dates", response = Custody.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 404, message = "The requested offender or conviction was not found.")
    })
    @ApiOperation(value = "Replaces all key dates specified in body. Key dates are either added or replaced or deleted if absent (see ReplaceCustodyKeyDates for the list). The the custodial conviction must be active")
    @PreAuthorize("hasRole('ROLE_COMMUNITY_CUSTODY_UPDATE')")
    public Custody replaceAllCustodyKeyDateByNomsNumberAndBookingNumber(final @PathVariable String nomsNumber,
                                                                        final @PathVariable String bookingNumber,
                                                                        final @RequestBody ReplaceCustodyKeyDates replaceCustodyKeyDates) {
        log.info("Call to replaceAllCustodyKeyDateByNomsNumberAndBookingNumber for {} booking {} with dates {}", nomsNumber, bookingNumber, replaceCustodyKeyDates);

        var offenderId = offenderService.offenderIdOfNomsNumber(nomsNumber).orElseThrow(() -> new NotFoundException(String.format("Offender with NOMS number %s not found", nomsNumber)));
        try {
            var convictionId = convictionService.getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(offenderId, bookingNumber).orElseThrow(() -> new NotFoundException(String.format("Conviction with bookingNumber %s not found for offender with NOMS number %s", bookingNumber, nomsNumber)));
            return convictionService.addOrReplaceOrDeleteCustodyKeyDates(offenderId, convictionId, replaceCustodyKeyDates);
        } catch (DuplicateConvictionsForBookingNumberException e) {
            log.warn("Multiple active custodial convictions found for {} for offender {}", bookingNumber, nomsNumber);
            throw new NotFoundException(String.format("Single active conviction for %s with booking number %s not found. Instead has %d convictions", nomsNumber, bookingNumber, e.getConvictionCount()));
        }
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/custody/keyDates/{typeCode}", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The new or updated custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be added to an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Adds or replaces a custody key date for the active custodial conviction")
    public CustodyKeyDate putCustodyKeyDateByOffenderId(final @PathVariable Long offenderId,
                                                        final @PathVariable String typeCode,
                                                        final @RequestBody CreateCustodyKeyDate custodyKeyDate) {
        log.info("Call to putCustodyKeyDateByOffenderId for {} code {}", offenderId, typeCode);
        return addOrReplaceCustodyKeyDate(offenderService.getOffenderByOffenderId(offenderId).map(OffenderDetail::getOffenderId), typeCode, custodyKeyDate);
    }

    @RequestMapping(value = "offenders/prisonBookingNumber/{prisonBookingNumber}/custody/keyDates/{typeCode}", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The new or updated custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be added to an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested conviction with associated prison booking was not found.")
    })
    @ApiOperation(value = "Adds or replaces a custody key date for the active custodial conviction")
    public CustodyKeyDate putCustodyKeyDateByPrisonBookingNumber(final @PathVariable String prisonBookingNumber,
                                                                 final @PathVariable String typeCode,
                                                                 final @RequestBody CreateCustodyKeyDate custodyKeyDate) {
        log.info("Call to putCustodyKeyDateByPrisonBookingNumber for {} code {}", prisonBookingNumber, typeCode);
        try {
            return addOrReplaceCustodyKeyDateByConvictionId(convictionService.getConvictionIdByPrisonBookingNumber(prisonBookingNumber), typeCode, custodyKeyDate);
        } catch (DuplicateConvictionsForBookingNumberException e) {
            log.warn("Multiple active custodial convictions found for {}", prisonBookingNumber);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can not add a key date where offender has multiple convictions for booking number. %s has %d", prisonBookingNumber, e.getConvictionCount()));
        }
    }


    @RequestMapping(value = "offenders/crn/{crn}/custody/keyDates/{typeCode}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be retrieved for an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found or does not have the supplied key date type.")
    })
    @ApiOperation(value = "Gets a custody key date for the active custodial conviction")
    public CustodyKeyDate getCustodyKeyDateByCrn(final @PathVariable String crn,
                                                 final @PathVariable String typeCode) {
        log.info("Call to getCustodyKeyDateByCrn for {} code {}", crn, typeCode);
        return getCustodyKeyDate(offenderService.offenderIdOfCrn(crn), typeCode);
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates/{typeCode}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be retrieved for an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found or does not have the supplied key date type.")
    })
    @ApiOperation(value = "Gets a custody key date for the active custodial conviction")
    public CustodyKeyDate getCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber,
                                                        final @PathVariable String typeCode) {
        log.info("Call to getCustodyKeyDateByNomsNumber for {} code {}", nomsNumber, typeCode);
        return getCustodyKeyDate(offenderService.offenderIdOfNomsNumber(nomsNumber), typeCode);
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/custody/keyDates/{typeCode}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be retrieved for an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found or does not have the supplied key date type.")
    })
    @ApiOperation(value = "Gets a custody key date for the active custodial conviction")
    public CustodyKeyDate getCustodyKeyDateByOffenderId(final @PathVariable Long offenderId,
                                                        final @PathVariable String typeCode) {
        log.info("Call to getCustodyKeyDateByOffenderId for {} code {}", offenderId, typeCode);
        return getCustodyKeyDate(offenderService.getOffenderByOffenderId(offenderId).map(OffenderDetail::getOffenderId), typeCode);
    }

    @RequestMapping(value = "offenders/prisonBookingNumber/{prisonBookingNumber}/custody/keyDates/{typeCode}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key date", response = CustodyKeyDate.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be retrieved for an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found or does not have the supplied key date type.")
    })
    @ApiOperation(value = "Gets a custody key date for the related custodial conviction with the matching prison booking")
    public CustodyKeyDate getCustodyKeyDateByPrisonBookingNumber(final @PathVariable String prisonBookingNumber,
                                                                 final @PathVariable String typeCode) {
        log.info("Call to getCustodyKeyDateByPrisonBookingNumber for {} code {}", prisonBookingNumber, typeCode);
        try {
            return getCustodyKeyDateByConvictionId(convictionService.getConvictionIdByPrisonBookingNumber(prisonBookingNumber), typeCode);
        } catch (DuplicateConvictionsForBookingNumberException e) {
            log.warn("Multiple active custodial convictions found for {}", prisonBookingNumber);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can only get a key date where offender has multiple convictions for booking number. %s has %d", prisonBookingNumber, e.getConvictionCount()));
        }
    }


    @RequestMapping(value = "offenders/crn/{crn}/custody/keyDates", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key dates", response = CustodyKeyDate.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The the offender does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Gets a all custody key dates for the active custodial conviction")
    public List<CustodyKeyDate> getAllCustodyKeyDateByCrn(final @PathVariable String crn) {
        log.info("Call to getAllCustodyKeyDateByCrn for {}", crn);
        return getCustodyKeyDates(offenderService.offenderIdOfCrn(crn));
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key dates", response = CustodyKeyDate.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The the offender does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Gets a all custody key dates for the active custodial conviction")
    public List<CustodyKeyDate> getAllCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber) {
        log.info("Call to getAllCustodyKeyDateByNomsNumber for {}", nomsNumber);
        return getCustodyKeyDates(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/custody/keyDates", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key dates", response = CustodyKeyDate.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The the offender does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Gets a all custody key dates for the active custodial conviction")

    public List<CustodyKeyDate> getAllCustodyKeyDateByOffenderId(final @PathVariable Long offenderId) {
        log.info("Call to getAllCustodyKeyDateByOffenderId for {}", offenderId);
        return getCustodyKeyDates(offenderService.getOffenderByOffenderId(offenderId).map(OffenderDetail::getOffenderId));
    }

    @RequestMapping(value = "offenders/prisonBookingNumber/{prisonBookingNumber}/custody/keyDates", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The custody key dates", response = CustodyKeyDate.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The the offender does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Gets a all custody key dates for the active custodial conviction")
    public List<CustodyKeyDate> getAllCustodyKeyDateByPrisonBookingNumber(final @PathVariable String prisonBookingNumber) {
        log.info("Call to getAllCustodyKeyDateByPrisonBookingNumber for {}", prisonBookingNumber);
        try {
            return getCustodyKeyDatesByConvictionId(convictionService.getConvictionIdByPrisonBookingNumber(prisonBookingNumber));
        } catch (DuplicateConvictionsForBookingNumberException e) {
            log.warn("Multiple active custodial convictions found for {}", prisonBookingNumber);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can only get a key dates where offender has multiple convictions for booking number. %s has %d", prisonBookingNumber, e.getConvictionCount()));
        }
    }

    @RequestMapping(value = "offenders/crn/{crn}/custody/keyDates/{typeCode}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Key date has been deleted"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be deleted from an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Deletes the custody key date for the active custodial conviction")
    public void deleteCustodyKeyDateByCrn(final @PathVariable String crn,
                                          final @PathVariable String typeCode) {
        log.info("Call to deleteCustodyKeyDateByCrn for {} code {}", crn, typeCode);
        deleteCustodyKeyDate(offenderService.offenderIdOfCrn(crn), typeCode);
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/custody/keyDates/{typeCode}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Key date has been deleted"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be deleted from an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Deletes the custody key date for the active custodial conviction")

    public void deleteCustodyKeyDateByNomsNumber(final @PathVariable String nomsNumber,
                                                 final @PathVariable String typeCode) {
        log.info("Call to deleteCustodyKeyDateByNomsNumber for {} code {}", nomsNumber, typeCode);
        deleteCustodyKeyDate(offenderService.offenderIdOfNomsNumber(nomsNumber), typeCode);
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/custody/keyDates/{typeCode}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Key date has been deleted"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid or a key date can not be deleted from an offender which does not have a single custody event"),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @ApiOperation(value = "Deletes the custody key date for the active custodial conviction")
    public void deleteCustodyKeyDateByOffenderId(final @PathVariable Long offenderId,
                                                 final @PathVariable String typeCode) {
        log.info("Call to deleteCustodyKeyDateByOffenderId for {} code {}", offenderId, typeCode);
        deleteCustodyKeyDate(offenderService.getOffenderByOffenderId(offenderId).map(OffenderDetail::getOffenderId), typeCode);
    }

    @RequestMapping(value = "offenders/prisonBookingNumber/{prisonBookingNumber}/custody/keyDates/{typeCode}", method = RequestMethod.DELETE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Key date has been deleted"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 400, message = "The keyDate is not valid"),
            @ApiResponse(code = 404, message = "The requested prison booking was not found.")
    })
    @ApiOperation(value = "Deletes the custody key date for the associated custodial conviction")
    public void deleteCustodyKeyDateByPrisonBookingNumber(final @PathVariable String prisonBookingNumber,
                                                          final @PathVariable String typeCode) {
        log.info("Call to deleteCustodyKeyDateByPrisonBookingNumber for {} code {}", prisonBookingNumber, typeCode);
        try {
            deleteCustodyKeyDateByConvictionId(convictionService.getConvictionIdByPrisonBookingNumber(prisonBookingNumber), typeCode);
        } catch (DuplicateConvictionsForBookingNumberException e) {
            log.warn("Multiple active custodial convictions found for {}", prisonBookingNumber);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can only delete a key date where offender has multiple convictions for booking number. %s has %d", prisonBookingNumber, e.getConvictionCount()));
        }
    }

    private CustodyKeyDate addOrReplaceCustodyKeyDate(Optional<Long> maybeOffenderId, String typeCode, CreateCustodyKeyDate custodyKeyDate) {
        return maybeOffenderId
                .map(offenderId -> {
                    try {
                        return convictionService.addOrReplaceCustodyKeyDateByOffenderId(offenderId, typeCode, custodyKeyDate);
                    } catch (CustodyTypeCodeIsNotValidException e) {
                        log.warn("Key type code is not valid for {}", typeCode);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found"));
    }

    private CustodyKeyDate addOrReplaceCustodyKeyDateByConvictionId(Optional<Long> maybeConvictionId, String typeCode, CreateCustodyKeyDate custodyKeyDate) {
        return maybeConvictionId
                .map(convictionId -> {
                    try {
                        return convictionService.addOrReplaceCustodyKeyDateByConvictionId(convictionId, typeCode, custodyKeyDate);
                    } catch (CustodyTypeCodeIsNotValidException e) {
                        log.warn("Key type code is not valid for {}", typeCode);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conviction not found"));
    }

    private CustodyKeyDate getCustodyKeyDate(Optional<Long> maybeOffenderId, String typeCode) {
        return maybeOffenderId
                .map(getCustodyKeyDateByOffenderId(typeCode))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found"));
    }

    private Function<Long, CustodyKeyDate> getCustodyKeyDateByOffenderId(String typeCode) {
        return offenderId -> convictionService.getCustodyKeyDateByOffenderId(offenderId, typeCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Key date not found"));
    }

    private CustodyKeyDate getCustodyKeyDateByConvictionId(Optional<Long> maybeConvictionId, String typeCode) {
        return maybeConvictionId
                .map(getCustodyKeyDateByConvictionId(typeCode))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conviction not found"));
    }

    private Function<Long, CustodyKeyDate> getCustodyKeyDateByConvictionId(String typeCode) {
        return convictionId ->
                convictionService.getCustodyKeyDateByConvictionId(convictionId, typeCode)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Key date not found"));
    }

    private List<CustodyKeyDate> getCustodyKeyDates(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(convictionService::getCustodyKeyDatesByOffenderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found"));
    }

    private List<CustodyKeyDate> getCustodyKeyDatesByConvictionId(Optional<Long> maybeConvictionId) {
        return maybeConvictionId
                .map(convictionService::getCustodyKeyDatesByConvictionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conviction not found"));
    }

    private void deleteCustodyKeyDate(Optional<Long> maybeOffenderId, String typeCode) {
        if (maybeOffenderId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found");
        }
        maybeOffenderId.ifPresent(offenderId -> convictionService.deleteCustodyKeyDateByOffenderId(offenderId, typeCode));
    }

    private void deleteCustodyKeyDateByConvictionId(Optional<Long> maybeConvictionId, String typeCode) {
        if (maybeConvictionId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offender not found");
        }
        maybeConvictionId.ifPresent(convictionId -> convictionService.deleteCustodyKeyDateByConvictionId(convictionId, typeCode));
    }

}
