package uk.gov.justice.digital.delius.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstance;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.PersonalCircumstanceService;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@Api(description = "Offender personal circumstance resources", tags = "Offender personalCircumstances")
public class PersonalCircumstanceController {
    private final OffenderService offenderService;
    private final PersonalCircumstanceService personalCircumstanceService;

    @Autowired
    public PersonalCircumstanceController(OffenderService offenderService, PersonalCircumstanceService personalCircumstanceService) {
        this.offenderService = offenderService;
        this.personalCircumstanceService = personalCircumstanceService;
    }

    @RequestMapping(value = "offenders/offenderId/{offenderId}/personalCircumstances", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<PersonalCircumstance>> getOffenderPersonalCircumstancesByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                                   final @PathVariable("offenderId") Long offenderId) {

        log.info("Call to getOffenderPersonalCircumstancesByOffenderId");
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return personalCircumstancesResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @RequestMapping(value = "offenders/nomsNumber/{nomsNumber}/personalCircumstances", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<PersonalCircumstance>> getOffenderPersonalCircumstancesByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("nomsNumber") String nomsNumber) {

        log.info("Call to getOffenderPersonalCircumstancesByNomsNumber");
        return personalCircumstancesResponseEntityOf(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @RequestMapping(value = "offenders/crn/{crn}/personalCircumstances", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<PersonalCircumstance>> getOffenderPersonalCircumstancesByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("crn") String crn) {

        log.info("Call to getOffenderPersonalCircumstancesByCrn");
        return personalCircumstancesResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    private ResponseEntity<List<PersonalCircumstance>> personalCircumstancesResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
            .map(offenderId -> new ResponseEntity<>(personalCircumstanceService.personalCircumstancesFor(offenderId), HttpStatus.OK))
            .orElseGet(this::notFound);
    }

    private ResponseEntity<List<PersonalCircumstance>> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
