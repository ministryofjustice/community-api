package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CaseNoteService {

    private final RestTemplate restTemplate;

    @Autowired
    public CaseNoteService(@Qualifier("deliusRestTemplateWithAuth") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> upsertCaseNotesToDelius(final String nomisId, final Long caseNotesId, final String caseNote) {

        return restTemplate.exchange("/nomisCaseNotes/{nomisId}/{caseNotesId}", HttpMethod.PUT, new HttpEntity<>(caseNote), String.class, nomisId, caseNotesId);
    }
}
