package uk.gov.justice.digital.delius.controller.wiremock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DeliusApiExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    private DeliusApiMockServer deliusApiMockServer;

    public DeliusApiExtension(DeliusApiMockServer deliusApiMockServer) {
        this.deliusApiMockServer = deliusApiMockServer;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        deliusApiMockServer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        deliusApiMockServer.stop();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        deliusApiMockServer.resetRequests();
    }

}
