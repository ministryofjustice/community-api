package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasic;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtAppearanceServiceTest {

    private static final String CRN = "X123456";
    private static final Long OFFENDER_ID = 99L;
    private static final Long EVENT_ID = 50L;

    @InjectMocks
    private CourtAppearanceService courtAppearanceService;

    @Mock
    private CourtAppearanceRepository courtAppearanceRepository;

    @BeforeEach
    void setUp() {
        courtAppearanceService = new CourtAppearanceService(courtAppearanceRepository);
    }

    private AdditionalOffence anAdditionalOffence(long id) {
        return AdditionalOffence.builder()
            .additionalOffenceId(id)
            .build();
    }

    private MainOffence aMainOffence(long id) {
        return MainOffence.builder()
            .mainOffenceId(id)
            .build();
    }

    @Test
    void softDeletedRecordsAreExcluded() {
        var deletedCourtAppearance = aCourtAppearance(0L, 1L, Collections.emptyList());
        var courtAppearance = aCourtAppearance(2L, 0L, ImmutableList.of(anAdditionalOffence(200L), anAdditionalOffence(201L)));

        when(courtAppearanceRepository.findByOffenderId(OFFENDER_ID))
            .thenReturn(ImmutableList.of(deletedCourtAppearance, courtAppearance));

        List<CourtAppearance> courtAppearances = courtAppearanceService.courtAppearancesFor(OFFENDER_ID);

        assertThat(courtAppearances).extracting("courtAppearanceId")
            .containsOnly(2L);
        assertThat(courtAppearances.get(0).getOffenceIds()).hasSize(3);
        assertThat(courtAppearances.get(0).getOffenceIds()).containsOnly("M1", "A200", "A201");
    }

    @Test
    void givenMultipleAppearancesForOffenderAndEvent_whenGetAppearances_thenSortByDate() {

        var now = LocalDateTime.now();
        var previous = now.minusDays(3);
        var appearance1 = aCourtAppearance(1L, 0L, Collections.emptyList(), now.minusDays(3));
        var appearance2 = aCourtAppearance(1L, 0L, Collections.emptyList(), now);
        var deletedAppearance = aCourtAppearance(1L, 1L, Collections.emptyList(), previous);

        when(courtAppearanceRepository.findByOffenderIdAndEventId(OFFENDER_ID, EVENT_ID))
            .thenReturn(ImmutableList.of(appearance1, appearance2, deletedAppearance));

        List<CourtAppearanceBasic> courtAppearances = courtAppearanceService.courtAppearancesFor(OFFENDER_ID, EVENT_ID);

        assertThat(courtAppearances).hasSize(2);
        assertThat(courtAppearances.get(0).getAppearanceDate()).isEqualTo(now);
        assertThat(courtAppearances.get(1).getAppearanceDate()).isEqualTo(previous);
    }

    @Test
    void shouldGetAppearancesByDateSortByDateAscending() {

        var today = LocalDate.now();
        var now = LocalDateTime.now();
        var previous = now.minusDays(3);
        var appearance1 = aCourtAppearance(1L, 0L, Collections.emptyList(), now.minusDays(3));
        var appearance2 = aCourtAppearance(1L, 0L, Collections.emptyList(), now);
        var deletedAppearance = aCourtAppearance(1L, 1L, Collections.emptyList(), previous);

        when(courtAppearanceRepository.findByAppearanceDateGreaterThanEqual(today.atStartOfDay()))
            .thenReturn(ImmutableList.of(appearance1, appearance2, deletedAppearance));

        List<CourtAppearanceBasic> courtAppearances = courtAppearanceService.courtAppearances(today);

        assertThat(courtAppearances).hasSize(2);
        assertThat(courtAppearances.get(0).getAppearanceDate()).isEqualTo(previous);
        assertThat(courtAppearances.get(1).getAppearanceDate()).isEqualTo(now);
    }

    private Court aCourt() {
        return Court.builder()
            .courtId(1L)
            .build();
    }

    private uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance aCourtAppearance(long appearanceId,
                                                                                                long softDeleted,
                                                                                                List<AdditionalOffence> additionalOffences) {
        return aCourtAppearance(appearanceId, softDeleted, additionalOffences, LocalDateTime.now());
    }

    private uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance aCourtAppearance(long appearanceId,
                                                                                                long softDeleted,
                                                                                                List<AdditionalOffence> additionalOffences,
                                                                                                LocalDateTime appearanceDate) {
        return uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance.builder()
            .courtAppearanceId(appearanceId)
            .softDeleted(softDeleted)
            .appearanceDate(appearanceDate)
            .offenderId(OFFENDER_ID)
            .offender(Offender.builder().offenderId(OFFENDER_ID).crn(CRN).build())
            .appearanceType(StandardReference.builder().codeDescription("appearance type").build())
            .event(Event
                .builder()
                .eventId(EVENT_ID)
                .mainOffence(aMainOffence(1L))
                .additionalOffences(additionalOffences)
                .build())
            .court(aCourt())
            .courtReports(ImmutableList.of(
                CourtReport.builder()
                    .courtReportId(1L)
                    .build()
            )).build();
    }

}
