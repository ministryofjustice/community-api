package uk.gov.justice.digital.delius.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.delius.data.api.ContactReport;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.Optional;

@RestController
@Log
public class ContactController {

    private final OffenderService offenderService;
    private final ContactService contactService;

    @Autowired
    public ContactController(OffenderService offenderService, ContactService contactService) {
        this.offenderService = offenderService;
        this.contactService = contactService;
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/contacts", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<ContactReport> getOffenderContactReportByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                              final @PathVariable("offenderId") Long offenderId) {
        return contactService.contactReportFor(offenderId)
                .map(contactReport -> new ResponseEntity<>(contactReport, HttpStatus.OK))
                .orElse(notFound());
    }

    private ResponseEntity<ContactReport> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/contacts", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<ContactReport> getOffenderReportContactByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                       final @PathVariable("crn") String crn) {

        return contactReportResponseEntityOf(offenderService.offenderIdOfCrn(crn));

    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/contacts", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<ContactReport> getOffenderContactReportByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                              final @PathVariable("nomsNumber") String nomsNumber) {

        return contactReportResponseEntityOf(offenderService.offenderIdOfNomsNumber(nomsNumber));

    }

    private ResponseEntity<ContactReport> contactReportResponseEntityOf(Optional<Long> aLong) {
        return aLong
                .flatMap(contactService::contactReportFor)
                .map(contactReport -> new ResponseEntity<>(contactReport, HttpStatus.OK))
                .orElseGet(this::notFound);
    }

}