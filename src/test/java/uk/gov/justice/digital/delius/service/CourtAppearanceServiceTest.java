package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.repository.AdditionalOffenceRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import({CourtAppearanceService.class, CourtAppearanceTransformer.class})
public class CourtAppearanceServiceTest {

    @Autowired
    private CourtAppearanceService courtAppearanceService;

    @MockBean
    private CourtAppearanceRepository courtAppearanceRepository;

    @MockBean
    private MainOffenceRepository mainOffenceRepository;

    @MockBean
    private AdditionalOffenceRepository additionalOffenceRepository;


    @Before
    public void setUp() {
        Mockito.when(courtAppearanceRepository.findByOffenderId(1L))
            .thenReturn(
                ImmutableList.of(
                    uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance.builder()
                        .courtAppearanceId(1L)
                        .softDeleted(1L)
                        .appearanceDate(LocalDateTime.now())
                        .offenderId(1L)
                        .eventId(50L)
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
                        .eventId(50L)
                        .court(aCourt())
                        .courtReports(ImmutableList.of(
                            uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                                .courtReportId(1L)
                                .build()
                        )).build()
                )
            );

        Mockito.when(mainOffenceRepository.findByEventId(50L))
            .thenReturn(
                ImmutableList.of(
                    MainOffence.builder()
                        .mainOffenceId(100L)
                        .build()
                )
            );

        Mockito.when(additionalOffenceRepository.findByEventId(50L))
            .thenReturn(
                ImmutableList.of(
                    AdditionalOffence.builder()
                        .additionalOffenceId(200L)
                        .build(),
                    AdditionalOffence.builder()
                        .additionalOffenceId(201L)
                        .build()
                )
            );
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