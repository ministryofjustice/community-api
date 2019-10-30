package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.jpa.oracle.annotations.NationalUserOverride;
import uk.gov.justice.digital.delius.service.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Api(description = "Offender resources protected by OAUTH2", tags = "Offenders (Secure)", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RestController
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class OffendersResource {

    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;
    private final DocumentService documentService;
    private final ContactService contactService;
    private final ConvictionService convictionService;

    @ApiOperation(
            value = "Return the responsible officer (RO) for an offender",
            notes = "Accepts a NOMIS offender nomsNumber in the format A9999AA")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = ResponsibleOfficer.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/responsibleOfficers")
    @NationalUserOverride
    public ResponseEntity<List<ResponsibleOfficer>> getResponsibleOfficersForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true) @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
            @ApiParam(name = "current", value = "Current only", example = "false", required = false) @RequestParam(name = "current", required = false, defaultValue = "false") final boolean current) {
        return offenderService.getResponsibleOfficersForNomsNumber(nomsNumber, current)
                .map(responsibleOfficer -> new ResponseEntity<>(responsibleOfficer, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiOperation(value = "Return the convictions (AKA Delius Event) for an offender")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = Conviction.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/convictions")
    @NationalUserOverride
    public ResponseEntity<List<Conviction>> getConvictionsForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
                .map(offenderId -> new ResponseEntity<>(convictionService.convictionsFor(offenderId), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Return the details for an offender")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = OffenderDetailSummary.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}")
    @NationalUserOverride
    public ResponseEntity<OffenderDetailSummary> getOffenderDetails(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {
        Optional<OffenderDetailSummary> offender = offenderService.getOffenderSummaryByNomsNumber(nomsNumber);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(new ResponseEntity<>(OffenderDetailSummary.builder().build(), NOT_FOUND));
    }

    @ApiOperation(value = "Returns all document's meta data for an offender")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = OffenderDocuments.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/documents/grouped")
    @NationalUserOverride
    public ResponseEntity<OffenderDocuments> getOffenderDocuments(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "A1234BB", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
                .map(offenderId -> new ResponseEntity<>(documentService.offenderDocumentsFor(offenderId), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Returns the document contents meta data for a given document associated with an offender")
    @GetMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents/{documentId}")
    @NationalUserOverride
    public HttpEntity<Resource> getOffenderDocument(
            final @PathVariable("nomsNumber") String nomsNumber,
            final @PathVariable("documentId") String documentId) {

        return offenderService.crnOf(nomsNumber)
                .map(crn -> alfrescoService.getDocument(documentId, crn))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiOperation(value = "Returns the contact details for an offender")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = Contact.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(value = "/offenders/nomsNumber/{nomsNumber}/contacts")
    @NationalUserOverride
    public ResponseEntity<List<Contact>> getOffenderContactReportByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                              final @PathVariable("nomsNumber") String nomsNumber,
                                                                              final @RequestParam("contactTypes") Optional<List<String>> contactTypes,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> from,
                                                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> to) {
        final ContactFilter contactFilter = ContactFilter.builder()
                .contactTypes(contactTypes)
                .from(from)
                .to(to)
                .build();

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
                .map(offenderId -> new ResponseEntity<>(contactService.contactsFor(offenderId, contactFilter), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

