package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CaseNoteService {

    private final RestTemplate restTemplate;

    @Autowired
    public CaseNoteService(@Qualifier("deliusRestTemplateWithAuth") final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> upsertCaseNotesToDelius(final String nomisId, final Long caseNotesId, final String caseNote) {
        final var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        return restTemplate.exchange("/nomisCaseNotes/{nomisId}/{caseNotesId}", HttpMethod.PUT, new HttpEntity<>(caseNote, headers), String.class, nomisId, caseNotesId);

    }
}
