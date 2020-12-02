package uk.gov.justice.digital.delius.controller.wiremock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AlfrescoExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    public static AlfrescoMockServer alfrescoMockServer = new AlfrescoMockServer(8088, "src/testIntegration/resources");

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        alfrescoMockServer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        alfrescoMockServer.stop();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        alfrescoMockServer.resetRequests();
    }

}
