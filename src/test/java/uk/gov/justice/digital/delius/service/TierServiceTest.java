package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import uk.gov.justice.digital.delius.jpa.standard.repository.StaffRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.TeamRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@ExtendWith(MockitoExtension.class)
public class TierServiceTest {

    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private ManagementTierRepository managementTierRepository;
    @Mock
    private ReferenceDataService referenceDataService;
    @Mock
    private ContactService contactService;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private SpgNotificationService spgNotificationService;

    private TierService service;

    @BeforeEach
    void setUp() {
        service = new TierService(managementTierRepository, telemetryClient, offenderRepository, referenceDataService, contactService, staffRepository, teamRepository, spgNotificationService);
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
                "tier", "U" + tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
            when(referenceDataService.getTier(String.format("U%s", tier))).thenReturn(Optional.of(new StandardReference()));
            when(referenceDataService.getAtsTierChangeReason()).thenReturn(Optional.of(new StandardReference()));
            when(staffRepository.findByOfficerCode(anyString())).thenReturn(Optional.of(aStaff()));
            when(teamRepository.findByCode(anyString())).thenReturn(Optional.of(aTeam()));
            service.updateTier(crn, tier);
            verify(telemetryClient).trackEvent("TierUpdateSuccess", telemetryProperties, null);
        }

        @Test
        @DisplayName("Creates SPG notification message")
        void createsSpgMessage() {
            String crn = "X123456";
            String tier = "A1";
            final var telemetryProperties = Map.of(
                "tier", "U" + tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
            when(referenceDataService.getTier(String.format("U%s", tier))).thenReturn(Optional.of(new StandardReference()));
            when(referenceDataService.getAtsTierChangeReason()).thenReturn(Optional.of(new StandardReference()));
            when(staffRepository.findByOfficerCode(anyString())).thenReturn(Optional.of(aStaff()));
            when(teamRepository.findByCode(anyString())).thenReturn(Optional.of(aTeam()));
            service.updateTier(crn, tier);
            verify(spgNotificationService).notifyUpdateOfOffender(offender.get());
        }

        @Test
        @DisplayName("falls back to offender manager if staff or team cannot be found. Remove once teams and staff have been added")
        void fallsbackToOffenderManager() {
            String crn = "X123456";
            String tier = "A1";
            final var telemetryProperties = Map.of(
                "tier", "U" + tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
            when(referenceDataService.getTier(String.format("U%s", tier))).thenReturn(Optional.of(new StandardReference()));
            when(referenceDataService.getAtsTierChangeReason()).thenReturn(Optional.of(new StandardReference()));
            service.updateTier(crn, tier);
            verify(telemetryClient).trackEvent("TierUpdateSuccess", telemetryProperties, null);
        }

        @Test
        @DisplayName("fires failure telemetry event when tier not found")
        void firesFailureTelemetryEventWhenTierNotFound() {
            String crn = "X123456";
            String tier = "NOTFOUND";
            final var telemetryProperties = Map.of(
                "tier", "U" + tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
            when(referenceDataService.getAtsTierChangeReason()).thenReturn(Optional.of(new StandardReference()));

            when(referenceDataService.getTier(String.format("U%s", tier))).thenReturn(Optional.ofNullable(null));
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
                "tier", "U" + tier, "crn", crn);
            Optional<Offender> offender = Optional.of(anOffender());
            when(offenderRepository.findByCrn(crn)).thenReturn(offender);
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
                "tier", "U" + tier, "crn", crn);
            when(offenderRepository.findByCrn(crn)).thenReturn(Optional.ofNullable(null));
            try {
                service.updateTier(crn, tier);
                fail("Should have thrown a NotFoundException");
            } catch (NotFoundException e) {
                verify(telemetryClient).trackEvent("TierUpdateFailureOffenderNotFound", telemetryProperties, null);
            }
        }

        @Test
        @DisplayName("fires failure telemetry event when offender manager not found")
        void firesFailureTelemetryEventWhenOffenderManagerNotFound() {
            String crn = "NOTFOUND";
            String tier = "A1";
            final var telemetryProperties = Map.of(
                "tier", "U" + tier, "crn", crn);
            when(offenderRepository.findByCrn(crn)).thenReturn(Optional.of(Offender.builder().offenderManagers(List.of(anInactiveOffenderManager("somecode"))).build()));
            when(referenceDataService.getTier(String.format("U%s", tier))).thenReturn(Optional.of(new StandardReference()));
            when(referenceDataService.getAtsTierChangeReason()).thenReturn(Optional.of(new StandardReference()));

            try {
                service.updateTier(crn, tier);
                fail("Should have thrown a NotFoundException");
            } catch (NotFoundException e) {
                verify(telemetryClient).trackEvent("TierUpdateFailureActiveCommunityOffenderManagerNotFound", telemetryProperties, null);
            }
        }

        @Nested
        @DisplayName("writeContact")
        @Disabled("until the workaround to use offender manager is removed")
        class WriteContact {
            @Test
            @DisplayName("fires failure telemetry event when staff not found")
            void firesFailureTelemetryEventWhenStaffNotFound() {
                String crn = "X123456";
                String tier = "A2";
                final var telemetryProperties = Map.of(
                    "tier", "U" + tier, "crn", crn);
                Optional<Offender> offender = Optional.of(anOffender());
                when(offenderRepository.findByCrn(crn)).thenReturn(offender);
                when(referenceDataService.getAtsTierChangeReason()).thenReturn(Optional.of(new StandardReference()));
                when(referenceDataService.getTier(String.format("U%s", tier))).thenReturn(Optional.ofNullable(new StandardReference()));

                try {
                    service.updateTier(crn, tier);
                    fail("Should have thrown a NotFoundException");
                } catch (NotFoundException e) {
                    verify(telemetryClient).trackEvent("TierUpdateFailureStaffNotFound", telemetryProperties, null);
                }
            }

            @Test
            @DisplayName("fires failure telemetry event when team not found")
            void firesFailureTelemetryEventWhenTeamNotFound() {
                String crn = "X123456";
                String tier = "A2";
                final var telemetryProperties = Map.of(
                    "tier", "U" + tier, "crn", crn);
                Optional<Offender> offender = Optional.of(anOffender());
                when(offenderRepository.findByCrn(crn)).thenReturn(offender);
                when(referenceDataService.getAtsTierChangeReason()).thenReturn(Optional.of(new StandardReference()));
                when(referenceDataService.getTier(String.format("U%s", tier))).thenReturn(Optional.ofNullable(new StandardReference()));
                when(staffRepository.findByOfficerCode(anyString())).thenReturn(Optional.of(aStaff()));
                try {
                    service.updateTier(crn, tier);
                    fail("Should have thrown a NotFoundException");
                } catch (NotFoundException e) {
                    verify(telemetryClient).trackEvent("TierUpdateFailureTeamNotFound", telemetryProperties, null);
                }
            }
        }
    }


}
