package uk.gov.justice.digital.delius.controller.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.http.HttpStatus;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public class DeliusMockServer extends WireMockServer {

    public DeliusMockServer(final int port) {
        super(WireMockConfiguration.wireMockConfig().port(port));
    }

    public DeliusMockServer(final int port, final String fileDirectory) {
        super(WireMockConfiguration.wireMockConfig().port(port).usingFilesUnderDirectory(fileDirectory).jettyStopTimeout(10000L));
    }

    public void stubPutCaseNoteToDeliusCreated(final String nomisId, final Long caseNotesId) {
        final var putCaseNote = "/nomisCaseNotes/" + nomisId + "/" + caseNotesId;
        stubFor(put(urlPathMatching(putCaseNote)).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(201)
            .withBody(" XXXX (crn) had a Contact created.")
        ));
    }

    public void stubPutCaseNoteToDeliusError(final String nomisId, final long caseNotesId, final HttpStatus status) {
        final var putCaseNote = "/nomisCaseNotes/" + nomisId + "/" + caseNotesId;
        stubFor(put(urlPathMatching(putCaseNote)).willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
        ));
    }
}
