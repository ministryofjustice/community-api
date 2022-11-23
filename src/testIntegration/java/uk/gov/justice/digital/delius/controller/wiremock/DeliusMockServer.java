package uk.gov.justice.digital.delius.controller.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class DeliusMockServer extends WireMockServer {

    public DeliusMockServer(final int port, final String fileDirectory) {
        super(WireMockConfiguration.wireMockConfig().port(port).usingFilesUnderDirectory(fileDirectory).jettyStopTimeout(10000L));
    }
}
