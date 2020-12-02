package uk.gov.justice.digital.delius.controller.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.http.HttpHeaders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static java.lang.String.format;

public class AlfrescoMockServer extends WireMockServer {

    public AlfrescoMockServer(final int port, final String fileDirectory) {
        super(WireMockConfiguration.wireMockConfig().port(port).usingFilesUnderDirectory(fileDirectory).jettyStopTimeout(10000L));
    }

    public void stubDetailsSuccess(final String documentId, final String crn, final String documentName) {
        stubFor(get(urlMatching(format("/alfresco/s/noms-spg/details/%s", documentId)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withHeader(HttpHeaders.CONTENT_DISPOSITION, format("attachment; filename=\\%s\\", documentId))
                        .withStatus(200)
                        .withBody(format("{ \"crn\": \"%s\", \"name\": \"%s\" }", crn, documentName))
                ));
    }

    public void stubFetchDocument(final String documentId, final byte[] body) {
        stubFor(get(urlMatching(format("/alfresco/s/noms-spg/fetch/%s", documentId)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withStatus(200)
                        .withBody(body)
                ));
    }
}
