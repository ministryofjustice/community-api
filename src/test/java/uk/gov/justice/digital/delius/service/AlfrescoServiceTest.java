package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.delius.data.api.alfresco.DocumentMeta;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoServiceTest {

    private AlfrescoService alfrescoService;
    @Mock
    private RestTemplate restTemplate;

    @Before
    public void before() {
        alfrescoService = new AlfrescoService(restTemplate, "user", "password");
    }

    @Test
    public void shouldBeNotFoundIfDocumentDOesNotBelongToOffender() {
        ResponseEntity<DocumentMeta> documentMeta = new ResponseEntity<>(
                DocumentMeta
                        .builder()
                        .crn("T1234")
                        .name("document.pdf")
                        .build(),
                HttpStatus.OK);
        when(restTemplate.exchange(eq("/details/123"), eq(HttpMethod.GET), any(HttpEntity.class), eq(DocumentMeta.class))).thenReturn(documentMeta);

        assertThat(alfrescoService.getDocument("123", "X9999").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    public void shouldReturnResourceWhenFound() throws IOException {
        ResponseEntity<DocumentMeta> documentMeta = new ResponseEntity<>(
                DocumentMeta
                        .builder()
                        .crn("T1234")
                        .name("document.pdf")
                        .build(),
                HttpStatus.OK);
        when(restTemplate.exchange(eq("/details/123"), eq(HttpMethod.GET), any(HttpEntity.class), eq(DocumentMeta.class))).thenReturn(documentMeta);
        when(restTemplate.exchange(eq("/fetch/123"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Resource.class)))
                .thenReturn(new ResponseEntity<>(new ByteArrayResource(new byte[]{'a', 'b', 'c'}), HttpStatus.OK));

        final ResponseEntity<Resource> response = alfrescoService.getDocument("123", "T1234");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().contentLength()).isEqualTo(3);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=\"document.pdf\"");
    }
}
