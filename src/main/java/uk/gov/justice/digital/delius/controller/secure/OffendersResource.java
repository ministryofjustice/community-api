package uk.gov.justice.digital.delius.controller.secure;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.ErrorResponse;
import uk.gov.justice.digital.delius.data.api.Contact;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jpa.filters.ContactFilter;
import uk.gov.justice.digital.delius.service.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Api(tags = "Offender resources (Secure)", authorizations = {@Authorization("ROLE_COMMUNITY")})
@RestController
@Slf4j
@RequestMapping(value = "secure", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@PreAuthorize("hasRole('ROLE_COMMUNITY')")
public class OffendersResource {

    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;
    private final DocumentService documentService;
    private final ContactService contactService;
    private final ConvictionService convictionService;
    private final OffenderManagerService offenderManagerService;

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
    public ResponseEntity<List<ResponsibleOfficer>> getResponsibleOfficersForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true) @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber,
            @ApiParam(name = "current", value = "Current only", example = "false") @RequestParam(name = "current", required = false, defaultValue = "false") final boolean current) {
        return offenderService.getResponsibleOfficersForNomsNumber(nomsNumber, current)
                .map(responsibleOfficer -> new ResponseEntity<>(responsibleOfficer, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiOperation(
            value = "Returns the current community and prison offender managers for an offender",
            notes = "Accepts a NOMIS offender nomsNumber in the format A9999AA")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = CommunityOrPrisonOffenderManager.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/allOffenderManagers")
    public List<CommunityOrPrisonOffenderManager> getAllOffenderManagersForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
            @NotNull
            @PathVariable(value = "nomsNumber")
            final String nomsNumber) {
        return offenderManagerService.getAllOffenderManagersForNomsNumber(nomsNumber)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with NOMS number %s not found", nomsNumber)));
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
    public ResponseEntity<List<Conviction>> getConvictionsForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
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
    public ResponseEntity<OffenderDetailSummary> getOffenderDetails(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {
        Optional<OffenderDetailSummary> offender = offenderService.getOffenderSummaryByNomsNumber(nomsNumber);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(new ResponseEntity<>(OffenderDetailSummary.builder().build(), NOT_FOUND));
    }

    @ApiOperation(value = "Returns all document's meta data for an offender by NOMS number")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = OffenderDocuments.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/documents/grouped")
    public ResponseEntity<OffenderDocuments> getOffenderDocuments(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
            @NotNull @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return offenderService.offenderIdOfNomsNumber(nomsNumber)
                .map(offenderId -> new ResponseEntity<>(documentService.offenderDocumentsFor(offenderId), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Returns all documents' meta data for an offender by CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderDocuments.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Not Found. For example if the CRN is not known.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path = "/offenders/crn/{crn}/documents/grouped")
    public ResponseEntity<OffenderDocuments> getOffenderDocumentsByCrn(
        @ApiParam(name = "crn", value = "CRN for the offender", example = "X340906", required = true)
        @NotNull @PathVariable(value = "crn") final String crn) {

        return offenderService.offenderIdOfCrn(crn)
            .map(offenderId -> new ResponseEntity<>(documentService.offenderDocumentsFor(offenderId), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Returns the document contents meta data for a given document associated with an offender")
    @GetMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents/{documentId}")
    public HttpEntity<Resource> getOffenderDocument(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true) @NotNull final @PathVariable("nomsNumber") String nomsNumber,
            @ApiParam(name = "documentId", value = "Document Id", example = "12312322", required = true) @NotNull final @PathVariable("documentId") String documentId) {

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
    public ResponseEntity<List<Contact>> getOffenderContactReportByNomsNumber(@ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true) @NotNull final @PathVariable("nomsNumber") String nomsNumber,
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

    @ApiOperation(
            value = "Returns the latest recall and release details for an offender",
            notes = "Accepts a NOMIS offender nomsNumber in the format A9999AA")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = OffenderLatestRecall.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/nomsNumber/{nomsNumber}/release")
    public OffenderLatestRecall getLatestRecallAndReleaseForOffender(
            @ApiParam(name = "nomsNumber", value = "Nomis number for the offender", example = "G9542VP", required = true)
            @NotNull
            @PathVariable(value = "nomsNumber") final String nomsNumber) {

        return getOffenderLatestRecall(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @ApiOperation(
            value = "Returns the latest recall and release details for an offender",
            notes = "Accepts an offender CRN in the format A999999")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = OffenderLatestRecall.class),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/release")
    public OffenderLatestRecall getLatestRecallAndReleaseForOffenderByCrn(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "X320741", required = true)
            @NotNull
            @PathVariable(value = "crn") final String crn) {

        return getOffenderLatestRecall(offenderService.offenderIdOfCrn(crn));
    }

    private OffenderLatestRecall getOffenderLatestRecall(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(offenderId -> offenderService.getOffenderLatestRecall(maybeOffenderId.get()))
                .orElseThrow(() -> new NotFoundException("Offender not found"));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/prisonOffenderManager", method = RequestMethod.PUT, consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The new prison offender manager", response = CommunityOrPrisonOffenderManager.class),
            @ApiResponse(code = 400, message = "Staff code does belong to the probation area related prison institution"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 404, message = "The offender or prison institution is not found")
    })
    @ApiOperation(value = "Allocates the prison offender manager for an offender in custody. This operation may also have a side affect of creating a Staff member if one matching the name does not already exist. An existing staff member can be used if the staff code is supplied.")
    public CommunityOrPrisonOffenderManager allocatePrisonOffenderManagerByNomsNumber(final @PathVariable String nomsNumber,
                                                                 final @RequestBody CreatePrisonOffenderManager prisonOffenderManager) {
        log.info("Request to allocate a prison offender manager to {} at prison with code {}", nomsNumber, prisonOffenderManager.getNomsPrisonInstitutionCode());

        Optional<String> errorMessage = prisonOffenderManager.validate();
        if (errorMessage.isPresent()) {
            throw new InvalidAllocatePOMRequestException(prisonOffenderManager, errorMessage.get());
        }

        return Optional.ofNullable(prisonOffenderManager.getOfficerCode())
                .map(staffCode -> offenderManagerService.allocatePrisonOffenderManagerByStaffCode(nomsNumber, staffCode, prisonOffenderManager))
                .orElseGet(() -> offenderManagerService.allocatePrisonOffenderManagerByName(nomsNumber, prisonOffenderManager))
                .orElseThrow(() -> new NotFoundException(String.format("Offender with noms number %s not found", nomsNumber)));
    }

    public static class InvalidAllocatePOMRequestException extends BadRequestException {
        InvalidAllocatePOMRequestException(CreatePrisonOffenderManager createPrisonOffenderManager, String message) {
            super(message);
            log.warn("Bad request: " + createPrisonOffenderManager);
        }
    }

    @RequestMapping(value = "/offenders/crn/{crn}", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The offender summary", response = OffenderDetailSummary.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender not found")
    })
    @ApiOperation(value = "Returns the offender summary for the given crn")

    public OffenderDetailSummary getOffenderSummaryByCrn(final @PathVariable("crn") String crn) {
        Optional<OffenderDetailSummary> offender = offenderService.getOffenderSummaryByCrn(crn);
        return offender.orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/all", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The offender details", response = OffenderDetail.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender is not found")
    })
    @ApiOperation(value = "Returns the full offender detail for the given crn")
    public OffenderDetail getOffenderDetailByCrn(final @PathVariable("crn") String crn) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByCrn(crn);
        return offender.orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/all", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The offender details", response = OffenderDetail.class),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender is not found")
    })
    @ApiOperation(value = "Returns the full offender detail for the given nomsNumber")
    public OffenderDetail getOffenderDetailByNomsNumber(final @PathVariable("nomsNumber") String nomsNumber) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByNomsNumber(nomsNumber);
        return offender.orElseThrow(() -> new NotFoundException(String.format("Offender with nomsNumber %s not found", nomsNumber)));
    }

    @ApiOperation(value = "Return the convictions (AKA Delius Event) for an offender")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "OK", response = Conviction.class, responseContainer = "List"),
                    @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
                    @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
                    @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
                    @ApiResponse(code = 404, message = "The offender is not found", response = ErrorResponse.class),
                    @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
            })
    @GetMapping(path = "/offenders/crn/{crn}/convictions")
    public List<Conviction> getConvictionsForOffenderByCrn(
            @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
            @NotNull @PathVariable(value = "crn") final String crn) {

        return offenderService.offenderIdOfCrn(crn)
                .map(convictionService::convictionsFor)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)));
    }

    @ApiOperation(value = "Return the conviction (AKA Delius Event) for a conviction ID and a CRN")
    @ApiResponses(
        value = {
            @ApiResponse(code = 200, message = "OK", response = Conviction.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorised", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "The offender CRN or conviction ID is not found", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error whilst processing request.", response = ErrorResponse.class)
        })
    @GetMapping(path = "/offenders/crn/{crn}/convictions/{convictionId}")
    public Conviction getConvictionForOffenderByCrnAndConvictionId(
        @ApiParam(name = "crn", value = "CRN for the offender", example = "A123456", required = true)
        @NotNull @PathVariable(value = "crn") final String crn,
        @ApiParam(name = "crn", value = "ID for the conviction / event", example = "2500295345", required = true)
        @NotNull @PathVariable(value = "convictionId") final Long convictionId) {

        return offenderService.offenderIdOfCrn(crn)
            .map((offenderId) -> convictionService.convictionFor(offenderId, convictionId))
            .orElseThrow(() -> new NotFoundException(String.format("Offender with crn %s not found", crn)))
            .orElseThrow(() -> new NotFoundException(String.format("Conviction with ID %s for Offender with crn %s not found", convictionId, crn)));
    }
}

