package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.digital.delius.data.api.alfresco.DocumentMeta;
import uk.gov.justice.digital.delius.data.api.alfresco.SearchResult;

import java.util.Optional;

import static java.lang.String.format;

@Service
public class AlfrescoService {
    private final WebClient webClient;
    private final MultiValueMap<String, String> headers;

    @Autowired
    public AlfrescoService(@Qualifier("alfrescoWebClient") WebClient webClient,
                           @Value("${alfresco.X-DocRepository-Remote-User}") String alfrescoRemoteUser,
                           @Value("${alfresco.X-DocRepository-Real-Remote-User}") String alfrescoRealRemoteUser) {
        this.webClient = webClient;
        headers = new LinkedMultiValueMap<>();
        headers.add("X-DocRepository-Remote-User", alfrescoRemoteUser);
        headers.add("X-DocRepository-Real-Remote-User", alfrescoRealRemoteUser);
    }


    public SearchResult listDocuments(String crn) {

        return webClient.get().uri(format("/search/%s", crn))
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToMono(SearchResult.class)
                .block();
    }

    public Optional<DocumentMeta> getDocumentDetail(String documentId, String crn) {

        return webClient.get().uri(format("/details/%s", documentId))
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToMono(DocumentMeta.class)
                .blockOptional()
                .filter(documentMeta -> documentMeta.getCrn().equals(crn));
    }

    public ResponseEntity<Resource> getDocument(String documentId, String crn) {
        val maybeDocumentMeta = getDocumentDetail(documentId, crn);

        return maybeDocumentMeta
                .map(documentMeta -> getDocument(documentId, Optional.of(documentMeta.getName())))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private ResponseEntity<Resource> getDocument(String documentId, Optional<String> filename) {
        return webClient.get().uri(format("/fetch/%s", documentId))
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(Resource.class)
                .map(resource -> new ResponseEntity<>(
                        resource.getBody(),
                        collectDocumentResourceHeaders(resource.getHeaders(), documentId, filename),
                        resource.getStatusCode()))
                .block();
    }

    private HttpHeaders collectDocumentResourceHeaders(final HttpHeaders responseHeaders, final String documentId, final Optional<String> filename) {
        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.add(HttpHeaders.ACCEPT_RANGES, responseHeaders.getFirst(HttpHeaders.ACCEPT_RANGES));
        newHeaders.add(HttpHeaders.CONTENT_LENGTH, responseHeaders.getFirst(HttpHeaders.CONTENT_LENGTH));
        newHeaders.add(HttpHeaders.CONTENT_TYPE, responseHeaders.getFirst(HttpHeaders.CONTENT_TYPE));
        newHeaders.add(HttpHeaders.ETAG, responseHeaders.getFirst(HttpHeaders.ETAG));
        newHeaders.add(HttpHeaders.LAST_MODIFIED, responseHeaders.getFirst(HttpHeaders.LAST_MODIFIED));
        newHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename.orElse(documentId) + "\"");
        return newHeaders;
    }

    public ResponseEntity<Resource> getDocument(String documentId) {
        return getDocument(documentId, Optional.empty());
    }
}
