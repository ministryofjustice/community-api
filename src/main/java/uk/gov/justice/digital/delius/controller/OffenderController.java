package uk.gov.justice.digital.delius.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.justice.digital.delius.data.api.AccessLimitation;
import uk.gov.justice.digital.delius.data.api.Count;
import uk.gov.justice.digital.delius.data.api.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.OffenderIdsResource;
import uk.gov.justice.digital.delius.data.api.views.Views;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.jwt.Jwt;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.NoSuchUserException;
import uk.gov.justice.digital.delius.service.OffenderService;
import uk.gov.justice.digital.delius.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@Log
public class OffenderController {

    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;
    private final UserService userService;
    private final Jwt jwt;

    @Autowired
    public OffenderController(OffenderService offenderService, AlfrescoService alfrescoService, UserService userService, Jwt jwt) {
        this.offenderService = offenderService;
        this.alfrescoService = alfrescoService;
        this.userService = userService;
        this.jwt = jwt;
    }

    private ResponseEntity<OffenderDetail> getOffenderByOffenderId(@PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByOffenderId(offenderId);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(notFound());
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.OffenderOnly.class)
    public ResponseEntity<OffenderDetail> getOffenderByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("offenderId") Long offenderId) {
        return getOffenderByOffenderId(offenderId);
    }

    @RequestMapping(value = "/offenders/crn/{crn}", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.OffenderOnly.class)
    public ResponseEntity<OffenderDetail> getOffenderByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                           final @PathVariable("crn") String crn) {
        return getOffenderByCrn(crn);
    }

    private ResponseEntity<OffenderDetail> getOffenderByCrn(String crn) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByCrn(crn);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(notFound());
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.OffenderOnly.class)
    public ResponseEntity<OffenderDetail> getOffenderByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("nomsNumber") String nomsNumber) {
        return getOffenderByNomsNumber(nomsNumber);
    }

    private ResponseEntity<OffenderDetail> getOffenderByNomsNumber(String nomsNumber) {
        Optional<OffenderDetail> offender = offenderService.getOffenderByNomsNumber(nomsNumber);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(notFound());
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/all", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.FullFat.class)
    public ResponseEntity<OffenderDetail> getFullFatOffenderByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("offenderId") Long offenderId) {
        return getOffenderByOffenderId(offenderId);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/all", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.FullFat.class)
    public ResponseEntity<OffenderDetail> getFullFatOffenderByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("crn") String crn) {
        return getOffenderByCrn(crn);
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/all", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.FullFat.class)
    public ResponseEntity<OffenderDetail> getFullFatOffenderByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                         final @PathVariable("nomsNumber") String nomsNumber) {
        return getOffenderByNomsNumber(nomsNumber);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<DocumentMeta>> getOffenderDocumentListByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                           final @PathVariable("crn") String crn) {
        List<DocumentMeta> documentMetas = alfrescoService.listDocuments(crn).getDocuments().stream().map(doc -> DocumentMeta.builder()
                .id(doc.getId())
                .createdAt(doc.getCreationDate())
                .docType(doc.getDocType())
                .documentName(doc.getName())
                .entityType(doc.getEntityType())
                .lastModifiedAt(doc.getLastModifiedDate())
                .build()).collect(Collectors.toList());

        return new ResponseEntity<>(documentMetas, OK);
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/documents", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<DocumentMeta>> getOffenderDocumentListByOffenderId(final @RequestHeader HttpHeaders httpHeaders,
                                                                                  final @PathVariable("offenderId") Long offenderId) {

        return maybeDocumentMetasOf(offenderService.crnOf(offenderId))
                .map(documentMetas -> new ResponseEntity<>(documentMetas, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    private Optional<List<DocumentMeta>> maybeDocumentMetasOf(Optional<String> maybeCrn) {
        return maybeCrn.map(crn -> alfrescoService.listDocuments(crn).getDocuments().stream().map(doc -> DocumentMeta.builder()
                .id(doc.getId())
                .createdAt(doc.getCreationDate())
                .docType(doc.getDocType())
                .documentName(doc.getName())
                .entityType(doc.getEntityType())
                .lastModifiedAt(doc.getLastModifiedDate())
                .build()).collect(Collectors.toList()));
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/documents", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<DocumentMeta>> getOffenderDocumentListByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                                  final @PathVariable("nomsNumber") String nomsNumber) {

        return maybeDocumentMetasOf(offenderService.crnOf(nomsNumber))
                .map(documentMetas -> new ResponseEntity<>(documentMetas, OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents/{documentId}/detail", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<DocumentMeta> getOffenderDocumentDetailByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                       final @PathVariable("crn") String crn,
                                                                       final @PathVariable("documentId") String documentId) {
        return documentMetaResponseEntityOf(crn, documentId);
    }

    private ResponseEntity<DocumentMeta> documentMetaResponseEntityOf(String crn, String documentId) {
        return alfrescoService.getDocumentDetail(documentId, crn).map(detail -> new ResponseEntity<>(DocumentMeta.builder()
                .createdAt(detail.getCreationDate())
                .lastModifiedAt(detail.getLastModifiedDate())
                .docType(detail.getDocType())
                .documentName(detail.getName())
                .id(detail.getId())
                .entityType(detail.getEntityType())
                .build(), OK)).orElse(new ResponseEntity<>(NOT_FOUND));
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
                                                             final @PathVariable("documentId") String documentId
    ) {
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
    public ResponseEntity<AccessLimitation> checkUserAccessByCrn(final @RequestHeader HttpHeaders httpHeaders,
                                                                 final @PathVariable("crn") String crn) {
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByCrn(crn);

        return accessLimitationResponseEntityOf(httpHeaders, maybeOffender);
    }

    @RequestMapping(value = "/offenders/nomsNumber/{nomsNumber}/userAccess", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<AccessLimitation> checkUserAccessByNomsNumber(final @RequestHeader HttpHeaders httpHeaders,
                                                                        final @PathVariable("nomsNumber") String nomsNumber) {
        Optional<OffenderDetail> maybeOffender = offenderService.getOffenderByNomsNumber(nomsNumber);

        return accessLimitationResponseEntityOf(httpHeaders, maybeOffender);
    }

    private ResponseEntity<OffenderDetail> notFound() {
        return new ResponseEntity<>(OffenderDetail.builder().build(), NOT_FOUND);
    }


    @ExceptionHandler(JwtTokenMissingException.class)
    public ResponseEntity<String> missingJwt(JwtTokenMissingException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<String> badJwt(MalformedJwtException e) {
        return new ResponseEntity<>("Bad Token.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> expiredJwt(ExpiredJwtException e) {
        return new ResponseEntity<>("Expired Token.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> notMine(SignatureException e) {
        return new ResponseEntity<>("Invalid signature.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> restClientError(HttpClientErrorException e) {
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> restServerError(HttpServerErrorException e) {
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(NoSuchUserException.class)
    public ResponseEntity<String> noSuchUser(NoSuchUserException e) {
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

}
