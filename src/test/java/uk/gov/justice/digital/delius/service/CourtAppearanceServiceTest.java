package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;
import uk.gov.justice.digital.delius.transformers.CourtReportTransformer;
import uk.gov.justice.digital.delius.transformers.CourtTransformer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import({CourtAppearanceService.class, CourtAppearanceTransformer.class, CourtReportTransformer.class, CourtTransformer.class})
public class CourtAppearanceServiceTest {

    @Autowired
    private CourtAppearanceService courtAppearanceService;

    @MockBean
    private CourtAppearanceRepository courtAppearanceRepository;

    @Before
    public void setUp() {
        when(courtAppearanceRepository.findByOffenderId(1L))
            .thenReturn(
                ImmutableList.of(
                    uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance.builder()
                        .courtAppearanceId(1L)
                        .softDeleted(1L)
                        .appearanceDate(LocalDateTime.now())
                        .offenderId(1L)
                        .event(Event
                                .builder()
                                .eventId(50L)
                                .mainOffence(aMainOffence(1L))
                                .build())
                        .court(aCourt())
                        .courtReports(ImmutableList.of(
                            uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                                .courtReportId(1L)
                                .build()
                        )).build(),
                    uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance.builder()
                        .courtAppearanceId(2L)
                        .appearanceDate(LocalDateTime.now())
                        .offenderId(1L)
                            .event(Event
                                    .builder()
                                    .eventId(50L)
                                    .mainOffence(aMainOffence(100L))
                                    .additionalOffences(ImmutableList.of(anAdditionalOffence(200L), anAdditionalOffence(201L)))
                                    .build())
                            .court(aCourt())
                        .courtReports(ImmutableList.of(
                            uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                                .courtReportId(1L)
                                .build()
                        )).build()
                )
            );

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
    public void softDeletedRecordsAreExcluded() {

        List<CourtAppearance> courtAppearances = courtAppearanceService.courtAppearancesFor(1L);

        assertThat(courtAppearances).extracting("courtAppearanceId")
            .containsOnly(2L);

        assertThat(courtAppearances.get(0).getOffenceIds()).containsOnly("M100", "A200", "A201");

    }

    private Court aCourt() {
        return Court.builder()
            .courtId(1L)
            .build();
    }

}