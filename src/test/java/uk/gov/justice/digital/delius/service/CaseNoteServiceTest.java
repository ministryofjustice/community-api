package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceTest {

    private CaseNoteService caseNoteService;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        caseNoteService = new CaseNoteService((restTemplate));
    }

    @Test
    public void shouldSendCaseNoteToDelius() {

        caseNoteService.upsertCaseNotesToDelius("54321", 12345L, "{\"content\":\"Bob\"}");

        verify(restTemplate).put("/nomisCaseNotes/{nomisId}/{caseNotesId}", "{\"content\":\"Bob\"}", "54321", 12345L);
    }

}
