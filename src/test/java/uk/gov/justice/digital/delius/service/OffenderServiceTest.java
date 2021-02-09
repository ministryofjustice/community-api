package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import io.vavr.control.Either;
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
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderPrimaryIdentifiersRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
class OffenderServiceTest {
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private OffenderPrimaryIdentifiersRepository offenderPrimaryIdentifiersRepository;
    @Mock
    private ConvictionService convictionService;
    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private StandardReferenceRepository standardReferenceRepository;

    private OffenderService service;

    @BeforeEach
    void setUp() {
        service = new OffenderService(offenderRepository, offenderPrimaryIdentifiersRepository, convictionService, standardReferenceRepository, telemetryClient);
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
            when(standardReferenceRepository.findByCodeAndCodeSetName(tier, "TIER")).thenReturn(Optional.of(new StandardReference()));
            when(offenderRepository.save(offender.get())).thenReturn(anOffender());
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
            when(standardReferenceRepository.findByCodeAndCodeSetName(tier, "TIER")).thenReturn(Optional.ofNullable(null));
            try {
                service.updateTier(crn, tier);
                fail("Should have thrown a NotFoundException");
            } catch (NotFoundException e) {
                verify(telemetryClient).trackEvent("TierUpdateFailureTierNotFound", telemetryProperties, null);
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

    @Nested
    @DisplayName("mostLikelyOffenderIdOfNomsNumber")
    class MostLikelyOffenderIdOfNomsNumber {
        @Test
        @DisplayName("will return offender id of the most likely offender")
        void willReturnOffenderIdOfTheMostLikelyOffender() {
            when(offenderRepository.findMostLikelyByNomsNumber(any()))
                .thenReturn(Either.right(Optional.of(anOffender().toBuilder().offenderId(99L)
                    .build())));

            assertThat(service.mostLikelyOffenderIdOfNomsNumber("A1234ZZ").get()).hasValue(99L);
        }

        @Test
        @DisplayName("will return empty if no offender found")
        void willReturnEmptyWhenNoFoundFund() {
            when(offenderRepository.findMostLikelyByNomsNumber(any()))
                .thenReturn(Either.right(Optional.empty()));

            assertThat(service.mostLikelyOffenderIdOfNomsNumber("A1234ZZ").get()).isEmpty();
        }

        @Test
        @DisplayName("will return error if duplicates found")
        void willReturnAnErrorForDuplicates() {
            when(offenderRepository.findMostLikelyByNomsNumber(any()))
                .thenReturn(Either.left(new OffenderRepository.DuplicateOffenderException("two found!")));

            assertThat(service.mostLikelyOffenderIdOfNomsNumber("A1234ZZ").isLeft()).isTrue();
        }

    }
}