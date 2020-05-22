package uk.gov.justice.digital.delius.controller.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

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
        final String putCaseNote = "/nomisCaseNotes/" + nomisId + "/" + caseNotesId;
        stubFor(put(urlPathMatching(putCaseNote)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(201)
                .withBody(" XXXX (crn) had a Contact created.")
        ));
    }

    public void stubPutCaseNoteToDeliusNoContent(final String nomisId, final Long caseNotesId) {
        final String putCaseNote = "/nomisCaseNotes/" + nomisId + "/" + caseNotesId;
        stubFor(put(urlPathMatching(putCaseNote)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(204)
        ));
    }

    public void stubPutCaseNoteToDeliusBadRequestError(final String nomisId, final long caseNotesId) {
        final String putCaseNote = "/nomisCaseNotes/" + nomisId + "/" + caseNotesId;
        stubFor(put(urlPathMatching(putCaseNote)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(400)
        ));
    }
}
