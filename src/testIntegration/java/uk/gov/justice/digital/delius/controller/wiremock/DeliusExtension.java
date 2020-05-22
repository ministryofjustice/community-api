package uk.gov.justice.digital.delius.controller.wiremock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DeliusExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

    private DeliusMockServer deliusMockServer;

    public DeliusExtension(DeliusMockServer deliusMockServer) {
        this.deliusMockServer = deliusMockServer;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        deliusMockServer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        deliusMockServer.stop();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        deliusMockServer.resetRequests();
    }

}
