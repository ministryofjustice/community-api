package uk.gov.justice.digital.delius.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.delius.data.api.*;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.DocumentService;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.*;

@RestController
@Slf4j
@Api(description = "Offender resources", tags = "Offenders")
public class OffenderController {

    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;
    private final DocumentService documentService;
    private final UserService userService;
    private final Jwt jwt;
    private final ObjectMapper objectMapper;

    @Autowired
    public OffenderController(OffenderService offenderService, AlfrescoService alfrescoService, DocumentService documentService, UserService userService, Jwt jwt, ObjectMapper objectMapper) {
        this.offenderService = offenderService;
        this.alfrescoService = alfrescoService;
        this.documentService = documentService;
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

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetailSummary> getOffenderSummaryByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                                final @PathVariable("nomsNumber") String nomsNumber) {
        Optional<OffenderDetailSummary> offender = offenderService.getOffenderSummaryByNomsNumber(nomsNumber);
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

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/all", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetail> getFullFatOffenderByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("nomsNumber") String nomsNumber) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByNomsNumber(nomsNumber);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailNotFound());

    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents", method = RequestMethod.GET)
    @ApiOperation(
            value = "Returns all offender related documents as a flat list given CRN",
            notes = "This uses Alfresco as it's source of information",
            nickname = "getOffenderDocumentListByNomsNumber")
    @JwtValidation
    public ResponseEntity<List<DocumentMeta>> getOffenderDocumentListByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                           final @PathVariable("crn") String crn) {
        List<DocumentMeta> documentMetas = alfrescoService.listDocuments(crn).getDocuments().stream().map(this::documentMetaOf).collect(Collectors.toList());

        return new ResponseEntity<>(documentMetas, OK);
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/documents", method = RequestMethod.GET)
    @ApiOperation(
            value = "Returns all offender related documents as a flat list given offenderId",
            notes = "This uses Alfresco as it's source of information",
            nickname = "getOffenderDocumentListByNomsNumber")
    @JwtValidation
    public ResponseEntity<List<DocumentMeta>> getOffenderDocumentListByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                  final @PathVariable("offenderId") Long offenderId) {

        return maybeDocumentMetasOf(offenderService.crnOf(offenderId))
                .map(documentMetas -> new ResponseEntity<>(documentMetas, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/release", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetail> getReleaseOffenderByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByOffenderId(offenderId);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailNotFound());
    }

    //    /offenders/crn/{crn}/release
    @RequestMapping(value = "/offenders/crn/{crn}/release", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetail> getReleaseFatOffenderByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                     final @PathVariable("crn") String crn) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByCrn(crn);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailNotFound());

    }

    //    /offenders/nomsNumber/{nomsNumber}/release
    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/release", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<OffenderDetail> getReleaseOffenderByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("nomsNumber") String nomsNumber) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByNomsNumber(nomsNumber);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(offenderDetailNotFound());

    }


    private Optional<List<DocumentMeta>> maybeDocumentMetasOf(Optional<String> maybeCrn) {
        return maybeCrn.map(crn -> alfrescoService.listDocuments(crn).getDocuments().stream().map(this::documentMetaOf).collect(Collectors.toList()));
    }

    private DocumentMeta documentMetaOf(uk.gov.justice.digital.delius.data.api.alfresco.DocumentMeta doc) {
        return DocumentMeta.builder()
                .id(doc.getId())
                .createdAt(doc.getCreationDate())
                .docType(doc.getDocType())
                .documentName(doc.getName())
                .entityType(doc.getEntityType())
                .lastModifiedAt(doc.getLastModifiedDate())
                .userData(Optional.ofNullable(doc.getUserData()).flatMap(userData -> {
                    try {
                        return Optional.ofNullable(objectMapper.readValue(userData, ObjectNode.class));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        return Optional.empty();
                    }
                }).orElse(null))
                .author(doc.getAuthor())
                .build();
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents", method = RequestMethod.GET)
    @ApiOperation(
            value = "Returns all offender related documents as a flat list given nomsNumber",
            notes = "This uses Alfresco as it's source of information",
            nickname = "getOffenderDocumentListByNomsNumber")
    @JwtValidation
    public ResponseEntity<List<DocumentMeta>> getOffenderDocumentListByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                                  final @PathVariable("nomsNumber") String nomsNumber) {

        return maybeDocumentMetasOf(offenderService.crnOf(nomsNumber))
                .map(documentMetas -> new ResponseEntity<>(documentMetas, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents/grouped", method = RequestMethod.GET)
    @ApiOperation(
            value = "Returns all offender related documents grouped e.g by conviction for a given nomsNumber",
            notes = "This uses the database as it's source of information",
            nickname = "getOffenderGroupedDocumentByNomsNumber")
    @JwtValidation
    public ResponseEntity<OffenderDocuments> getOffenderGroupedDocumentByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                                    final @PathVariable("nomsNumber") String nomsNumber) {
        log.info("Call to getOffenderGroupedDocumentByNomsNumber");
        return offenderDocumentsResponseEntityOf(offenderService.offenderIdOfNomsNumber(nomsNumber));
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/documents/grouped", method = RequestMethod.GET)
    @ApiOperation(
            value = "Returns all offender related documents grouped e.g by conviction for a given offenderId",
            notes = "This uses the database as it's source of information",
            nickname = "getOffenderGroupedDocumentByOffendeerId")
    @JwtValidation
    public ResponseEntity<OffenderDocuments> getOffenderGroupedDocumentByOffendeerId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                     final @PathVariable("offenderId") Long offenderId) {
        log.info("Call to getOffenderGroupedDocumentByOffendeerId");
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByOffenderId(offenderId);

        return offenderDocumentsResponseEntityOf(maybeOffender.map(OffenderDetail::getOffenderId));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents/grouped", method = RequestMethod.GET)
    @ApiOperation(
            value = "Returns all offender related documents grouped e.g by conviction for a given CRN",
            notes = "This uses the database as it's source of information",
            nickname = "getOffenderGroupedDocumentByCrn")
    @JwtValidation
    public ResponseEntity<OffenderDocuments> getOffenderGroupedDocumentByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                             final @PathVariable("crn") String crn) {
        log.info("Call to getOffenderGroupedDocumentByCrn");
        return offenderDocumentsResponseEntityOf(offenderService.offenderIdOfCrn(crn));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents/{documentId}/detail", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<DocumentMeta> getOffenderDocumentDetailByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                       final @PathVariable("crn") String crn,
                                                                       final @PathVariable("documentId") String documentId) {
        return documentMetaResponseEntityOf(crn, documentId);
    }

    private ResponseEntity<DocumentMeta> documentMetaResponseEntityOf(String crn, String documentId) {
        return alfrescoService.getDocumentDetail(documentId, crn).map(detail -> new ResponseEntity<>(documentMetaOf(detail), OK)).orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/documents/{documentId}/detail", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<DocumentMeta> getOffenderDocumentDetailByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                              final @PathVariable("offenderId") Long offenderId,
                                                                              final @PathVariable("documentId") String documentId) {

        return documentMetaResponseEntityOf(documentId, offenderService.crnOf(offenderId))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents/{documentId}/detail", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<DocumentMeta> getOffenderDocumentDetailByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                              final @PathVariable("nomsNumber") String nomsNumber,
                                                                              final @PathVariable("documentId") String documentId) {

        return documentMetaResponseEntityOf(documentId, offenderService.crnOf(nomsNumber))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    private Optional<ResponseEntity<DocumentMeta>> documentMetaResponseEntityOf(String documentId, Optional<String> maybeCrn) {
        return maybeCrn.map(crn -> documentMetaResponseEntityOf(crn, documentId));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents/{documentId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<Resource> getOffenderDocumentByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                             final @PathVariable("crn") String crn,
                                                             final @PathVariable("documentId") String documentId) {
        return alfrescoService.getDocument(documentId, crn);
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/documents/{documentId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<Resource> getOffenderDocumentByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                    final @PathVariable("offenderId") Long offenderId,
                                                                    final @PathVariable("documentId") String documentId
    ) {
        return offenderService.crnOf(offenderId)
                .map(crn -> alfrescoService.getDocument(documentId, crn))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents/{documentId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<Resource> getOffenderDocumentByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                    final @PathVariable("nomsNumber") String nomsNumber,
                                                                    final @PathVariable("documentId") String documentId
    ) {
        return offenderService.crnOf(nomsNumber)
                .map(crn -> alfrescoService.getDocument(documentId, crn))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/offenderIds", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<org.springframework.hateoas.Resource<OffenderIdsResource>> getOffenderIds(final @RequestHeader HttpHeaders httpHeaders,
                                                                                                    final @RequestParam(defaultValue = "${offender.ids.pagesize:1000}") int pageSize,
                                                                                                    final @RequestParam(defaultValue = "1") int page) {

        Link nextLink = linkTo(methodOn(OffenderController.class).getOffenderIds(httpHeaders, pageSize, page + 1)).withRel("next");


        List<BigDecimal> offenderIds = offenderService.allOffenderIds(pageSize, page);
        if (offenderIds.isEmpty()) {
            return new ResponseEntity<>(NOT_FOUND);
        }

        return new ResponseEntity<>(
                new org.springframework.hateoas.Resource<>(
                        OffenderIdsResource.builder().offenderIds(offenderIds).build(),
                        nextLink), OK);
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
        if (!maybeOffender.isPresent()) {
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

    @RequestMapping(value = "/offenders/crn/{crn}/userAccess", method = RequestMethod.GET)
    @JwtValidation
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User has unrestricted access to offender"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "User is restricted from access to offender", response = AccessLimitation.class),
            @ApiResponse(code = 404, message = "No such offender, or no such User (see body for detail)")
    })
    public ResponseEntity<AccessLimitation> checkUserAccessByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                 final @PathVariable("crn") String crn) {
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByCrn(crn);

        return accessLimitationResponseEntityOf(httpHeaders, maybeOffender);
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/userAccess", method = RequestMethod.GET)
    @JwtValidation
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User has unrestricted access to offender"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "User is restricted from access to offender", response = AccessLimitation.class),
            @ApiResponse(code = 404, message = "No such offender, or no such User (see body for detail)")
    })
    public ResponseEntity<AccessLimitation> checkUserAccessByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                        final @PathVariable("nomsNumber") String nomsNumber) {
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByNomsNumber(nomsNumber);

        return accessLimitationResponseEntityOf(httpHeaders, maybeOffender);
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/offenderManagers", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<OffenderManager>> getOffenderManagerByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                                final @PathVariable("nomsNumber") String nomsNumber) {

        return offenderService.getOffenderManagersForNomsNumber(nomsNumber)
                .map(offenderManager -> new ResponseEntity<>(offenderManager, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/offenderManagers", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<OffenderManager>> getOffenderManagerByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("crn") String crn) {
        return offenderService.getOffenderManagersForCrn(crn)
                .map(offenderManager -> new ResponseEntity<>(offenderManager, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/offenderManagers", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<OffenderManager>> getOffenderManagerByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                final @PathVariable("offenderId") Long offenderId) {
        return offenderService.getOffenderManagersForOffenderId(offenderId)
                .map(offenderManager -> new ResponseEntity<>(offenderManager, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/documents/{documentId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<Resource> getDocumentById(final @RequestHeader HttpHeaders httpHeaders,
                                                    final @PathVariable("documentId") String documentId
    ) {
        return alfrescoService.getDocument(documentId);
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/responsibleOfficers", method = RequestMethod.GET)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "A list of responsible officers for an offender", response = ResponsibleOfficer.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Request is missing Authorization header (no JWT)"),
            @ApiResponse(code = 403, message = "The requesting user was restricted from access", response = AccessLimitation.class),
            @ApiResponse(code = 404, message = "The requested offender was not found.")
    })
    @JwtValidation
    public ResponseEntity<List<ResponsibleOfficer>> getResponsibleOfficersByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                                       final @PathVariable("nomsNumber") String nomsNumber,
                                                                                       final @RequestParam(name = "current", required = false, defaultValue = "false") boolean current) {

        return offenderService.getResponsibleOfficersForNomsNumber(nomsNumber, current)
                .map(responsibleOfficer -> new ResponseEntity<>(responsibleOfficer, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    private ResponseEntity<OffenderDetail> offenderDetailNotFound() {
        return new ResponseEntity<>(OffenderDetail.builder().build(), NOT_FOUND);
    }

    private ResponseEntity<OffenderDetailSummary> offenderDetailSummaryNotFound() {
        return new ResponseEntity<>(OffenderDetailSummary.builder().build(), NOT_FOUND);
    }

    private ResponseEntity<OffenderDocuments> offenderDocumentsResponseEntityOf(Optional<Long> maybeOffenderId) {
        return maybeOffenderId
                .map(offenderId -> new ResponseEntity<>(documentService.offenderDocumentsFor(offenderId), HttpStatus.OK))
                .orElseGet(this::notFoundOffenderDocuments);
    }

    private ResponseEntity<OffenderDocuments> notFoundOffenderDocuments() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
