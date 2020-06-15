package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CaseNoteService {

    private final WebClient webClient;

    @Autowired
    public CaseNoteService(@Qualifier("deliusWebClientWithAuth") final WebClient webClient) {
        this.webClient = webClient;
    }

    public ResponseEntity<String> upsertCaseNotesToDelius(final String nomisId, final Long caseNotesId, final String caseNote) {
        return webClient.put()
                .uri(uriBuilder -> uriBuilder.path("/nomisCaseNotes/{nomisId}/{caseNotesId}").build(nomisId, caseNotesId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(caseNote)
                .retrieve()
                .toEntity(String.class)
                .block();
    }
}
