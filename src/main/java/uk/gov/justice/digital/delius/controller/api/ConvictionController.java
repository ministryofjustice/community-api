package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.ConvictionService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@Api(description = "Offender conviction resources", tags = "Offender convictions")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConvictionController {
    private final OffenderService offenderService;
    private final ConvictionService convictionService;

    @Autowired
    public ConvictionController(OffenderService offenderService, ConvictionService convictionService) {
        this.offenderService = offenderService;
        this.convictionService = convictionService;
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/convictions", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Conviction>> getOffenderConvictionsByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                                   final @PathVariable("offenderId") Long offenderId) {

        log.info("Call to getOffenderConvictionsByOffenderId");
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return convictionsResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @RequestMapping(value = "offenders/crn/{crn}/convictions", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Conviction>> getOffenderConvictionsByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("crn") String crn) {

        log.info("Call to getOffenderConvictionsByCrn");
        return convictionsResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    private ResponseEntity<List<Conviction>> convictionsResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new ResponseEntity<>(convictionService.convictionsFor(offenderId, false), HttpStatus.OK))
            .orElseGet(this::notFound);
    }

    private ResponseEntity<List<Conviction>> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
