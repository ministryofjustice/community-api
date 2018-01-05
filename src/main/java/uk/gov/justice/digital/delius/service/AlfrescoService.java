package uk.gov.justice.digital.delius.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.delius.data.api.alfresco.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.alfresco.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log
public class AlfrescoService {
    private final RestTemplate restTemplate;

    @Autowired
    public AlfrescoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SearchResult listDocuments(String crn) {
        ResponseEntity<SearchResult> forEntity = restTemplate.getForEntity("/search/" + crn, SearchResult.class);
        return forEntity.getBody();
    }

    public Optional<DocumentMeta> getDocumentDetail(String documentId, String crn) {
        ResponseEntity<DocumentMeta> forEntity = restTemplate.getForEntity("/details/" + documentId, DocumentMeta.class);

        if (forEntity.getBody().getCrn().equals(crn)) {
            return Optional.of(forEntity.getBody());
        }
        return Optional.empty();
    }

    public ResponseEntity<Resource> getDocument(String documentId, String crn) {

        // Check crn exists
        SearchResult searchResult = listDocuments(crn);

        // Check documentId belongs to crn
        if (!searchResult.hasDocumentId(documentId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.ALL);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        return restTemplate.exchange("/fetch/" + documentId, HttpMethod.GET, httpEntity, Resource.class);
    }

}