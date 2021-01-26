package uk.gov.justice.digital.delius.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "features")
@Data
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
    private NomsFeatures noms = new NomsFeatures();
    private boolean applyLimitedAccessMarkers;
}
