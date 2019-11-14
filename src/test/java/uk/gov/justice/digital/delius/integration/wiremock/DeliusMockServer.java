package uk.gov.justice.digital.delius.integration.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DeliusMockServer extends WireMockRule {

    private static final int WIREMOCK_PORT = 8999;

    public DeliusMockServer() {
        super(WIREMOCK_PORT);
    }

    public void stubPutCaseNoteToDelius(final String nomisId, final Long caseNotesId) {
        final String putCaseNote = "/nomisCaseNotes/" + nomisId + "/" + caseNotesId;
        stubFor(put(urlPathMatching(putCaseNote)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(204)
        ));
    }

    public void stubPutCaseNoteToDeliusNoContentError(final String nomisId, final long caseNotesId) {
        final String putCaseNote = "/nomisCaseNotes/" + nomisId + "/" + caseNotesId;
        stubFor(put(urlPathMatching(putCaseNote)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(400)
        ));
    }
}
