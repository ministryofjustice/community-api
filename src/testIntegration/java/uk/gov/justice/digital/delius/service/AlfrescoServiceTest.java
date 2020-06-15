package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.config.AlfrescoConfig;
import uk.gov.justice.digital.delius.controller.wiremock.AlfrescoExtension;
import uk.gov.justice.digital.delius.controller.wiremock.AlfrescoMockServer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AlfrescoConfig.class, AlfrescoService.class},
properties = {
        "alfresco.X-DocRepository-Remote-User=any_remote_user",
        "alfresco.X-DocRepository-Real-Remote-User=any_real_user",
        "alfresco.baseUrl=http://localhost:8099"
})
@AutoConfigureWebClient
public class AlfrescoServiceTest {

    private static final AlfrescoMockServer alfrescoMockServer = new AlfrescoMockServer(8099);

    @RegisterExtension
    static AlfrescoExtension alfrescoExtension = new AlfrescoExtension(alfrescoMockServer);

    @Autowired
    private AlfrescoService alfrescoService;

    @Test
    public void shouldBeNotFoundIfDocumentDoesNotBelongToOffender() {
        alfrescoMockServer.stubDetailsSuccess("123", "T1234", "document.pdf");

        assertThat(alfrescoService.getDocument("123", "NOT_T1234").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnResourceWhenFound() throws IOException {
        alfrescoMockServer.stubDetailsSuccess("123", "T1234", "document.pdf");
        alfrescoMockServer.stubFetchDocument("123", new byte[]{'a', 'b', 'c'});

        final var response = alfrescoService.getDocument("123", "T1234");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().contentLength()).isEqualTo(3);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=\"document.pdf\"");
    }

}
