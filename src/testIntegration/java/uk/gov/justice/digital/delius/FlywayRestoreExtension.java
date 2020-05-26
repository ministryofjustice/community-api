package uk.gov.justice.digital.delius;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class FlywayRestoreExtension implements AfterAllCallback, BeforeEachCallback {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(SpringExtension.class);
    private static Flyway flyway;
    private static Logger logger = LoggerFactory.getLogger(FlywayRestoreExtension.class);

    private static TestContextManager getTestContextManager(ExtensionContext context) {
        // copied from SpringExtension
        final var testClass = context.getRequiredTestClass();
        final var store = getStore(context);
        return store.getOrComputeIfAbsent(testClass, TestContextManager::new, TestContextManager.class);
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        logger.info("Cleaning database ..");
        flyway.clean();
        flyway.migrate();
        logger.info("Cleaning database done");
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        final var applicationContext = getTestContextManager(extensionContext).getTestContext()
                .getApplicationContext();
        // deal with Nested classes
        if (applicationContext.containsBean("flyway")) {
            flyway = applicationContext.getBean(Flyway.class);
        }
    }

}
