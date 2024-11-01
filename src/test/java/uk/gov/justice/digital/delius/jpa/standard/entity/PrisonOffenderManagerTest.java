package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrisonOffenderManagerTest {

    @Test
    @DisplayName("Will retrieve the single active responsible officer that is not end dated")
    void willRetrieveTheActiveResponsibleOfficer() {
        final var endDatedRO1 = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(1L)
                .endDateTime(LocalDateTime.now())
                .build();
        final var endDatedRO2 = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(2L)
                .endDateTime(LocalDateTime.now().minusDays(1))
                .build();
        final var notEndDatedRO = ResponsibleOfficer.builder().responsibleOfficerId(3L).endDateTime(null).build();
        final var activeResponsibleOfficer = PrisonOffenderManager
                .builder()
                .responsibleOfficers(List.of(endDatedRO1, endDatedRO2, notEndDatedRO))
                .build()
                .getActiveResponsibleOfficer();

        assertThat(activeResponsibleOfficer).isEqualTo(notEndDatedRO);
    }

    @Test
    @DisplayName("Will return null when there are no responsible officers")
    void willBeNullWhenEmpty() {
        final var prisonOffenderManager = PrisonOffenderManager.builder().responsibleOfficers(List.of()).build();
        assertThat(prisonOffenderManager.getActiveResponsibleOfficer()).isNull();
    }

    @Test
    @DisplayName("Will return null when there are no active responsible officers")
    void willBeNullWhenNotActive() {
        final var endDatedRO = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(1L)
                .endDateTime(LocalDateTime.now())
                .build();

        final var prisonOffenderManager = PrisonOffenderManager
                .builder()
                .responsibleOfficers(List.of(endDatedRO))
                .build();
        assertThat(prisonOffenderManager.getActiveResponsibleOfficer()).isNull();
    }

    @Test
    @DisplayName("Can add a responsible officer")
    void canAddAResponsibleOfficer() {
        final var prisonOffenderManager = new PrisonOffenderManager();
        final var responsibleOfficer = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(1L)
                .endDateTime(LocalDateTime.now())
                .build();

        prisonOffenderManager.addResponsibleOfficer(responsibleOfficer);
        assertThat(prisonOffenderManager.getResponsibleOfficers()).containsExactly(responsibleOfficer);
        assertThat(prisonOffenderManager.getActiveResponsibleOfficer()).isNull();
    }

    @Test
    @DisplayName("Will retrieve the latest responsible officer based on start datetime")
    void willRetrieveTheLatestResponsibleOfficer() {
        final var endDatedRO1 = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(1L)
                .endDateTime(LocalDateTime.now())
                .startDateTime(LocalDateTime.now().minusWeeks(1))
                .build();
        final var endDatedRO2 = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(2L)
                .endDateTime(LocalDateTime.now().minusDays(1))
                .startDateTime(LocalDateTime.now().minusDays(1))
                .build();
        final var notEndDatedRO = ResponsibleOfficer.builder()
                .responsibleOfficerId(3L)
                .startDateTime(LocalDateTime.now().minusMinutes(1))
                .endDateTime(null).build();

        final var latestResponsibleOfficer = PrisonOffenderManager
                .builder()
                .responsibleOfficers(List.of(endDatedRO1, endDatedRO2, notEndDatedRO))
                .build()
                .getLatestResponsibleOfficer();

        assertThat(latestResponsibleOfficer).isEqualTo(notEndDatedRO);
    }

    @Test
    @DisplayName("Will retrieve the latest responsible officer based on start datetime even if none are active")
    void willRetrieveTheLatestResponsibleOfficerEvenWhenNoneActive() {
        final var endDatedRO1 = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(1L)
                .endDateTime(LocalDateTime.now())
                .startDateTime(LocalDateTime.now().minusWeeks(1))
                .build();
        final var endDatedRO2 = ResponsibleOfficer
                .builder()
                .responsibleOfficerId(2L)
                .endDateTime(LocalDateTime.now().minusDays(1))
                .startDateTime(LocalDateTime.now().minusDays(1))
                .build();

        final var latestResponsibleOfficer = PrisonOffenderManager
                .builder()
                .responsibleOfficers(List.of(endDatedRO1, endDatedRO2))
                .build()
                .getLatestResponsibleOfficer();

        assertThat(latestResponsibleOfficer).isEqualTo(endDatedRO2);
    }


    @Test
    @DisplayName("Will return no latest ro when there are no responsible officers")
    void latestWillBeNullWhenEmpty() {
        final var prisonOffenderManager = PrisonOffenderManager.builder().responsibleOfficers(List.of()).build();
        assertThat(prisonOffenderManager.getLatestResponsibleOfficer()).isNull();
    }

}
