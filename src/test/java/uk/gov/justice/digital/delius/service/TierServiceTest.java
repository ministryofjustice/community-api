package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ManagementTierRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
public class TierServiceTest {

    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private StandardReferenceRepository standardReferenceRepository;
    @Mock
    private ManagementTierRepository managementTierRepository;

    private TierService service;

    @BeforeEach
    void setUp() {
        service = new TierService(managementTierRepository, standardReferenceRepository, telemetryClient, offenderRepository);
    }

    @Nested
    @DisplayName("updateTier")
    class UpdateTier {
        @Test
        @DisplayName("fires success telemetry event")
        void firesSuccessTelemetryEvent() {
            String crn = "X123456";
            String tier = "A1";
            final var telemetryProperties = Map.of(
                "tier", tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
            when(standardReferenceRepository.findByCodeAndCodeSetName(String.format("U%s",tier), "TIER")).thenReturn(Optional.of(new StandardReference()));
            when(standardReferenceRepository.findByCodeAndCodeSetName("ATS", "TIER CHANGE REASON")).thenReturn(Optional.of(new StandardReference()));
            service.updateTier(crn, tier);
            verify(telemetryClient).trackEvent("TierUpdateSuccess", telemetryProperties, null);
        }

        @Test
        @DisplayName("fires failure telemetry event when tier not found")
        void firesFailureTelemetryEventWhenTierNotFound() {
            String crn = "X123456";
            String tier = "NOTFOUND";
            final var telemetryProperties = Map.of(
                "tier", tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
            when(standardReferenceRepository.findByCodeAndCodeSetName(String.format("U%s",tier), "TIER")).thenReturn(Optional.ofNullable(null));
            try {
                service.updateTier(crn, tier);
                fail("Should have thrown a NotFoundException");
            } catch (NotFoundException e) {
                verify(telemetryClient).trackEvent("TierUpdateFailureTierNotFound", telemetryProperties, null);
            }
        }

        @Test
        @DisplayName("fires failure telemetry event when tier change reason not found")
        void firesFailureTelemetryEventWhenTierChangeReasonNotFound() {
            String crn = "X123456";
            String tier = "A2";
            final var telemetryProperties = Map.of(
                "tier", tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
            when(standardReferenceRepository.findByCodeAndCodeSetName(String.format("U%s",tier), "TIER")).thenReturn(Optional.of(new StandardReference()));
            try {
                service.updateTier(crn, tier);
                fail("Should have thrown a NotFoundException");
            } catch (NotFoundException e) {
                verify(telemetryClient).trackEvent("TierUpdateFailureTierChangeReasonNotFound", telemetryProperties, null);
            }
        }

        @Test
        @DisplayName("fires failure telemetry event when offender not found")
        void firesFailureTelemetryEventWhenOffenderNotFound() {
            String crn = "NOTFOUND";
            String tier = "A1";
            final var telemetryProperties = Map.of(
                "tier", tier, "crn", crn);
            when(offenderRepository.findByCrn(crn)).thenReturn(Optional.ofNullable(null));
            try {
                service.updateTier(crn, tier);
                fail("Should have thrown a NotFoundException");
            } catch (NotFoundException e) {
                verify(telemetryClient).trackEvent("TierUpdateFailureOffenderNotFound", telemetryProperties, null);
            }
        }
    }


}
