package uk.gov.justice.digital.delius;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class FlywayRestoreExtension implements AfterAllCallback, BeforeEachCallback {
    private static Flyway flyway;
    private static Logger logger = LoggerFactory.getLogger(FlywayRestoreExtension.class);

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        logger.info("Cleaning database ..");
        flyway.clean();
        flyway.migrate();
        logger.info("Cleaning database done");
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        final var applicationContext = SpringExtension.getApplicationContext(extensionContext);
        // deal with Nested classes
        if (applicationContext.containsBean("flyway")) {
            flyway = applicationContext.getBean(Flyway.class);
        }
    }

}
