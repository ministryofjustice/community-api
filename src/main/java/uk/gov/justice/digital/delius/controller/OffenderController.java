package uk.gov.justice.digital.delius.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.justice.digital.delius.data.api.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.OffenderDetail;
import uk.gov.justice.digital.delius.data.api.views.Views;
import uk.gov.justice.digital.delius.exception.JwtTokenMissingException;
import uk.gov.justice.digital.delius.jwt.JwtValidation;
import uk.gov.justice.digital.delius.service.AlfrescoService;
import uk.gov.justice.digital.delius.service.OffenderService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@Log
public class OffenderController {

    private final OffenderService offenderService;
    private final AlfrescoService alfrescoService;

    @Autowired
    public OffenderController(OffenderService offenderService, AlfrescoService alfrescoService) {
        this.offenderService = offenderService;
        this.alfrescoService = alfrescoService;
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.OffenderOnly.class)
    public ResponseEntity<OffenderDetail> getOffender(final @RequestHeader HttpHeaders httpHeaders,
                                                      final @PathVariable("offenderId") Long offenderId) {
        return offenderResponseOf(offenderId);
    }

    @RequestMapping(value = "/offenders/offenderId/{offenderId}/all", method = RequestMethod.GET)
    @JwtValidation
    @JsonView(Views.FullFat.class)
    public ResponseEntity<OffenderDetail> getFullFatOffender(final @RequestHeader HttpHeaders httpHeaders,
                                                             final @PathVariable("offenderId") Long offenderId) {
        return offenderResponseOf(offenderId);
    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<List<DocumentMeta>> getOffenderDocumentList(final @RequestHeader HttpHeaders httpHeaders,
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

    @RequestMapping(value = "/offenders/crn/{crn}/documents/{documentId}/detail", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<DocumentMeta> getOffenderDocumentDetail(final @RequestHeader HttpHeaders httpHeaders,
                                                                  final @PathVariable("crn") String crn,
                                                                  final @PathVariable("documentId") String documentId) {
        return alfrescoService.getDocumentDetail(documentId, crn).map(detail -> new ResponseEntity<>(DocumentMeta.builder()
                .createdAt(detail.getCreationDate())
                .lastModifiedAt(detail.getLastModifiedDate())
                .docType(detail.getDocType())
                .documentName(detail.getName())
                .id(detail.getId())
                .entityType(detail.getEntityType())
                .build(), OK)).orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(value = "/offenders/crn/{crn}/documents/{documentId}", method = RequestMethod.GET)
    @JwtValidation
    public ResponseEntity<Resource> getOffenderDocument(final @RequestHeader HttpHeaders httpHeaders,
                                                        final @PathVariable("crn") String crn,
                                                        final @PathVariable("documentId") String documentId
    ) {
        return alfrescoService.getDocument(documentId, crn);
    }

    private ResponseEntity<OffenderDetail> offenderResponseOf(@PathVariable("offenderId") Long offenderId) {
        Optional<OffenderDetail> offender = offenderService.getOffender(offenderId);
        return offender.map(
                offenderDetail -> new ResponseEntity<>(offenderDetail, OK)).orElse(notFound());
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

}
