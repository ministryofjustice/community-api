package uk.gov.justice.digital.delius.controller.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DeliusApiMockServer extends WireMockServer {

    public DeliusApiMockServer(final int port) {
        super(WireMockConfiguration.wireMockConfig().port(port));
    }

    public DeliusApiMockServer(final int port, final String fileDirectory) {
        super(WireMockConfiguration.wireMockConfig().port(port).usingFilesUnderDirectory(fileDirectory).jettyStopTimeout(10000L));
    }
    public void stubPostContactToDeliusApi() {
        stubFor(post(urlPathMatching("/v1/contact")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(201)
                .withBody("{\n" +
                    "    \"id\": 2503596167,\n" +
                    "    \"offenderCrn\": \"X371505\",\n" +
                    "    \"type\": \"COAP\",\n" +
                    "    \"provider\": \"N07\",\n" +
                    "    \"team\": \"N07CHT\",\n" +
                    "    \"staff\": \"N07A007\",\n" +
                    "    \"officeLocation\": \"LDN_BCS\",\n" +
                    "    \"date\": \"2021-06-02\",\n" +
                    "    \"startTime\": \"10:00:00\",\n" +
                    "    \"endTime\": \"12:00:00\",\n" +
                    "    \"alert\": false,\n" +
                    "    \"sensitive\": false,\n" +
                    "    \"eventId\": 2500428188,\n" +
                    "    \"requirementId\": 2500185174\n" +
                    "}")
        ));
    }
}
