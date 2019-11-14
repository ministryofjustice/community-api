package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CaseNoteService {

    private final RestTemplate restTemplate;

    @Autowired
    public CaseNoteService(@Qualifier("deliusRestTemplateWithAuth") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void upsertCaseNotesToDelius(final String nomisId, final Long caseNotesId, final String caseNote) {

        restTemplate.put("/nomisCaseNotes/{nomisId}/{caseNotesId}", caseNote, nomisId, caseNotesId);
    }
}
