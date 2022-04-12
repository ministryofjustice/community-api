package uk.gov.justice.digital.delius.config;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "features")
@Data
@Slf4j
public class FeatureSwitches {
    @Data
    public static class NomsFeatures {
        private CustodyUpdate update = new CustodyUpdate();
    }
    @Data
    public static class CustodyUpdate {
        private boolean custody;
        private OffenderNumber booking = new OffenderNumber();
        private boolean keyDates;
        private OffenderNumber noms = new OffenderNumber();
        private MultipleEvents multipleEvents = new MultipleEvents();
        private boolean releaseRecall;
    }

    @Data
    public static class MultipleEvents {
        private boolean updateBulkKeyDates;
        private boolean updateKeyDates;
        private boolean updatePrisonLocation;
    }
    @Data
    public static class OffenderNumber {
        private boolean number;
    }
    @Data
    public static class Registers {
        private String courtCodeAllowedPattern;
    }

    private NomsFeatures noms = new NomsFeatures();
    private Registers registers = new Registers();


    @PostConstruct
    private void postConstruct() {
        log.info("Feature switches set as " + this);
    }
}
