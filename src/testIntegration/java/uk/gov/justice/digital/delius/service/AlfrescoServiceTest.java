package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.config.AlfrescoConfig;
import uk.gov.justice.digital.delius.controller.wiremock.AlfrescoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AlfrescoConfig.class, AlfrescoService.class},
properties = {
        "alfresco.X-DocRepository-Remote-User=any_remote_user",
        "alfresco.X-DocRepository-Real-Remote-User=any_real_user"
})
@AutoConfigureWebClient
@ExtendWith(AlfrescoExtension.class)
public class AlfrescoServiceTest {

    @Autowired
    private AlfrescoService alfrescoService;

    @Test
    public void shouldBeNotFoundIfDocumentDoesNotBelongToOffender() {
        AlfrescoExtension.alfrescoMockServer.stubDetailsSuccess("123", "T1234", "document.pdf");

        assertThat(alfrescoService.getDocument("123", "NOT_T1234").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnResourceWhenFound() throws IOException {

        String filename = "document (1).pdf";

        AlfrescoExtension.alfrescoMockServer.stubDetailsSuccess("123", "T1234", filename);
        AlfrescoExtension.alfrescoMockServer.stubFetchDocument("123", new byte[]{'a', 'b', 'c'});

        final var response = alfrescoService.getDocument("123", "T1234");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().contentLength()).isEqualTo(3);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
            .isEqualTo("attachment; filename*=UTF-8''document%20%281%29.pdf");
    }

}
