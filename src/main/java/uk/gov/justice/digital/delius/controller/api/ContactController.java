package uk.gov.justice.digital.delius.controller.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.ContactService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@Api(description = "Offender contact resources", tags = "Offender Contacts")
@RequestMapping(value = "api", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public ResponseEntity<List<Contact>> getOffenderContactReportByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                              final @PathVariable("offenderId") Long offenderId,
                                                                              final @RequestParam("contactTypes") Optional<List<String>> contactTypes,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> from,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> to) {

        ContactFilter contactFilter = ContactFilter.builder()
                .contactTypes(contactTypes)
                .from(from)
                .to(to)
                .build();

        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);
        return contactsResponseEntityOf(maybeOffender.map(offenderDetail -> Optional.of(offenderDetail.getOffenderId())).orElse(Optional.empty()), contactFilter);
    }

    private ResponseEntity<List<Contact>> notFound() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/contacts", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Contact>> getOffenderReportContactByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                       final @PathVariable("crn") String crn,
                                                                       final @RequestParam("contactTypes") Optional<List<String>> contactTypes,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> from,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> to) {

        ContactFilter contactFilter = ContactFilter.builder()
                .contactTypes(contactTypes)
                .from(from)
                .to(to)
                .build();

        return contactsResponseEntityOf(offenderService.offenderIdOfCrn(crn), contactFilter);

    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/contacts", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<Contact>> getOffenderContactReportByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                              final @PathVariable("nomsNumber") String nomsNumber,
                                                                              final @RequestParam("contactTypes") Optional<List<String>> contactTypes,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> from,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> to) {

        ContactFilter contactFilter = ContactFilter.builder()
                .contactTypes(contactTypes)
                .from(from)
                .to(to)
                .build();

        return contactsResponseEntityOf(offenderService.offenderIdOfNomsNumber(nomsNumber), contactFilter);

    }

    private ResponseEntity<List<Contact>> contactsResponseEntityOf(Optional<Long> maybeOffenderId, ContactFilter filter) {
        return maybeOffenderId
                .map(offenderId -> new ResponseEntity<>(contactService.contactsFor(offenderId, filter), HttpStatus.OK))
                .orElseGet(this::notFound);
    }

}