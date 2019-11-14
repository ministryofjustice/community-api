package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.delius.data.api.alfresco.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.alfresco.SearchResult;

import java.util.Optional;

@Service
public class AlfrescoService {
    private final RestTemplate restTemplate;
    private final MultiValueMap<String, String> headers;

    @Autowired
    public AlfrescoService(@Qualifier("alfrescoRestTemplate")RestTemplate restTemplate,
                           @Value("${alfresco.X-DocRepository-Remote-User}") String alftresecoRemoteUser,
                           @Value("${alfresco.X-DocRepository-Real-Remote-User}") String alfrescoRealRemoteUser) {
        this.restTemplate = restTemplate;
        headers = new LinkedMultiValueMap<>();
        headers.add("X-DocRepository-Remote-User", alftresecoRemoteUser);
        headers.add("X-DocRepository-Real-Remote-User", alfrescoRealRemoteUser);
    }


    public SearchResult listDocuments(String crn) {


        ResponseEntity<SearchResult> forEntity = restTemplate.exchange("/search/" + crn,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                SearchResult.class);
        return forEntity.getBody();
    }

    public Optional<DocumentMeta> getDocumentDetail(String documentId, String crn) {
        ResponseEntity<DocumentMeta> forEntity = restTemplate.exchange("/details/" + documentId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DocumentMeta.class);

        if (forEntity.getBody().getCrn().equals(crn)) {
            return Optional.of(forEntity.getBody());
        }
        return Optional.empty();
    }

    public ResponseEntity<Resource> getDocument(String documentId, String crn) {
        val maybeDocumentMeta = getDocumentDetail(documentId, crn);

        return maybeDocumentMeta
                .map(documentMeta -> getDocument(documentId, Optional.of(documentMeta.getName())))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private ResponseEntity<Resource> getDocument(String documentId, Optional<String> filename) {
        ResponseEntity<Resource> forEntity = restTemplate.exchange("/fetch/" + documentId, HttpMethod.GET, new HttpEntity<>(headers),
                Resource.class);

        HttpHeaders responseHeaders = forEntity.getHeaders();
        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.add(HttpHeaders.ACCEPT_RANGES, responseHeaders.getFirst(HttpHeaders.ACCEPT_RANGES));
        newHeaders.add(HttpHeaders.CONTENT_LENGTH, responseHeaders.getFirst(HttpHeaders.CONTENT_LENGTH));
        newHeaders.add(HttpHeaders.CONTENT_TYPE, responseHeaders.getFirst(HttpHeaders.CONTENT_TYPE));
        newHeaders.add(HttpHeaders.ETAG, responseHeaders.getFirst(HttpHeaders.ETAG));
        newHeaders.add(HttpHeaders.LAST_MODIFIED, responseHeaders.getFirst(HttpHeaders.LAST_MODIFIED));
        newHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename.orElse(documentId) + "\"");


        ResponseEntity<Resource> response = new ResponseEntity<>(forEntity.getBody(), newHeaders, forEntity.getStatusCode());

        return response;

    }

    public ResponseEntity<Resource> getDocument(String documentId) {
        return getDocument(documentId, Optional.empty());
    }
}
