package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.aResponsibleOfficer;
import static uk.gov.justice.digital.delius.util.EntityHelper.anActiveOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anActivePrisonOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anInactiveOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anInactivePrisonOffenderManager;

class OffenderTest {
    private final PrisonOffenderManager inactivePOM = anInactivePrisonOffenderManager("ABC");
    private final PrisonOffenderManager activePOMNotRO = anActivePrisonOffenderManager().toBuilder()
            .responsibleOfficers(List.of()).build();
    private final PrisonOffenderManager activePOMIsRO = anActivePrisonOffenderManager().toBuilder()
            .responsibleOfficers(List.of(aResponsibleOfficer())).build();

    private final OffenderManager inactiveCOM = anInactiveOffenderManager("ABC");
    private final OffenderManager activeCOMNotRO = anActiveOffenderManager().toBuilder().responsibleOfficers(List.of())
            .build();
    private final OffenderManager activeCOMIsRO = anActiveOffenderManager().toBuilder()
            .responsibleOfficers(List.of(aResponsibleOfficer())).build();

    @Nested
    class GetResponsibleOfficerWhoIsPrisonOffenderManager {
        @Test
        void willReturnEmptyWhenNoPOMs() {
            final Offender offender = Offender.builder().prisonOffenderManagers(List.of()).build();
            assertThat(offender.getResponsibleOfficerWhoIsPrisonOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnEmptyWhenNoActivePOMs() {
            final Offender offender = Offender.builder().prisonOffenderManagers(List.of(inactivePOM)).build();
            assertThat(offender.getResponsibleOfficerWhoIsPrisonOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnEmptyWhenActivePOMIsNotAResponsibleOfficer() {
            final Offender offender = Offender.builder().prisonOffenderManagers(List.of(activePOMNotRO)).build();
            assertThat(offender.getResponsibleOfficerWhoIsPrisonOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnActivePOMWhoIsAlsoResponsibleOfficer() {
            final Offender offender = Offender.builder().prisonOffenderManagers(List.of(inactivePOM, activePOMIsRO))
                    .build();
            assertThat(offender.getResponsibleOfficerWhoIsPrisonOffenderManager()).get().isEqualTo(activePOMIsRO);
        }
    }

    @Nested
    class GetActiveCommunityOffenderManager {
        @Test
        void willReturnEmptyWhenNoCOMs() {
            final Offender offender = Offender.builder().offenderManagers(List.of()).build();
            assertThat(offender.getActiveCommunityOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnEmptyWhenNoActiveCOMs() {
            final Offender offender = Offender.builder().offenderManagers(List.of(inactiveCOM)).build();
            assertThat(offender.getActiveCommunityOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnActiveCOM() {
            final Offender offender = Offender.builder().offenderManagers(List.of(inactiveCOM, activeCOMNotRO)).build();
            assertThat(offender.getActiveCommunityOffenderManager()).get().isEqualTo(activeCOMNotRO);
        }

    }

    @Nested
    class GetResponsibleOfficerWhoIsCommunityOffenderManager {
        @Test
        void willReturnEmptyWhenNoCOMs() {
            final Offender offender = Offender.builder().offenderManagers(List.of()).build();
            assertThat(offender.getResponsibleOfficerWhoIsCommunityOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnEmptyWhenNoActiveCOMs() {
            final Offender offender = Offender.builder().offenderManagers(List.of(inactiveCOM)).build();
            assertThat(offender.getResponsibleOfficerWhoIsCommunityOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnEmptyWhenActiveCOMIsNotAResponsibleOfficer() {
            final Offender offender = Offender.builder().offenderManagers(List.of(activeCOMNotRO)).build();
            assertThat(offender.getResponsibleOfficerWhoIsCommunityOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnActiveCOMWhoIsAlsoResponsibleOfficer() {
            final Offender offender = Offender.builder().offenderManagers(List.of(inactiveCOM, activeCOMIsRO)).build();
            assertThat(offender.getResponsibleOfficerWhoIsCommunityOffenderManager()).get().isEqualTo(activeCOMIsRO);
        }
    }

    @Nested
    class GetActivePrisonOffenderManager {
        @Test
        void willReturnEmptyWhenNoPOMs() {
            final Offender offender = Offender.builder().prisonOffenderManagers(List.of()).build();
            assertThat(offender.getActivePrisonOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnEmptyWhenNoActivePOMs() {
            final Offender offender = Offender.builder().prisonOffenderManagers(List.of(inactivePOM)).build();
            assertThat(offender.getActivePrisonOffenderManager()).isNotPresent();
        }

        @Test
        void willReturnActivePOM() {
            final Offender offender = Offender.builder().prisonOffenderManagers(List.of(inactivePOM, activePOMNotRO))
                    .build();
            assertThat(offender.getActivePrisonOffenderManager()).get().isEqualTo(activePOMNotRO);
        }

    }
}