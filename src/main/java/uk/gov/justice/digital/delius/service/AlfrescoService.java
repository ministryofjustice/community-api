package uk.gov.justice.digital.delius.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    private final String alfresecoRemoteUser;
    private final String alfrescoRealRemoteUser;
    private final MultiValueMap<String, String> headers;

    @Autowired
    public AlfrescoService(RestTemplate restTemplate,
                           @Value("${alfresco.X-DocRepository-Remote-User}") String alftresecoRemoteUser,
                           @Value("${alfresco.X-DocRepository-Real-Remote-User}") String alfrescoRealRemoteUser) {
        this.restTemplate = restTemplate;
        this.alfresecoRemoteUser = alftresecoRemoteUser;
        this.alfrescoRealRemoteUser = alfrescoRealRemoteUser;
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

        // Check crn exists
        SearchResult searchResult = listDocuments(crn);

        // Check documentId belongs to crn
        if (!searchResult.hasDocumentId(documentId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ResponseEntity<Resource> forEntity = restTemplate.exchange("/fetch/" + documentId, HttpMethod.GET, new HttpEntity<>(headers),
                Resource.class);

        return forEntity;
    }

}