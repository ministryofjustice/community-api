package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.AdditionalOffenceRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalReportRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;
import uk.gov.justice.digital.delius.transformers.AdditionalOffenceTransformer;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;
import uk.gov.justice.digital.delius.transformers.CourtReportTransformer;
import uk.gov.justice.digital.delius.transformers.CourtTransformer;
import uk.gov.justice.digital.delius.transformers.InstitutionalReportTransformer;
import uk.gov.justice.digital.delius.transformers.MainOffenceTransformer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import({InstitutionalReportService.class, InstitutionalReportTransformer.class,
    MainOffenceTransformer.class, AdditionalOffenceTransformer.class, OffenceService.class,
    CourtAppearanceService.class, CourtAppearanceTransformer.class, CourtReportTransformer.class, CourtTransformer.class})
public class InstitutionalReportServiceTest {

    @Autowired
    private InstitutionalReportService institutionalReportService;

    @MockBean
    private InstitutionalReportRepository institutionalReportRepository;

    @MockBean
    private MainOffenceRepository mainOffenceRepository;

    @MockBean
    private AdditionalOffenceRepository additionalOffenceRepository;

    @MockBean
    private CourtAppearanceRepository courtAppearanceRepository;

    @Before
    public void setup() {
        when(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(1L, 1L)).
            thenReturn(Optional.of(InstitutionalReport.builder()
                .softDeleted(1L)
                .build()));

        when(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(2L, 2L)).
            thenReturn(Optional.of(anInstitutionalReportLinkedToAnEvent()));

        when(courtAppearanceRepository.findByEventId(42L)).
            thenReturn(Optional.of(CourtAppearance.builder()
                .outcome(StandardReference.builder()
                    .codeDescription("Some sentence text")
                    .build())
                .court(Court.builder().build())
                .courtReports(ImmutableList.of())
                .build()));

        when(institutionalReportRepository.findByOffenderId(3L)).
            thenReturn(ImmutableList.of(
                InstitutionalReport.builder()
                    .softDeleted(1L)
                    .build(),
                anInstitutionalReportLinkedToAnEvent(),
                InstitutionalReport.builder()
                    .softDeleted(0L)
                    .build())
            );

        when(mainOffenceRepository.findByEventId(42L))
            .thenReturn(ImmutableList.of(MainOffence.builder()
                .mainOffenceId(22L)
                .softDeleted(0L)
                .eventId(42L)
                .offence(Offence.builder()
                    .ogrsOffenceCategory(StandardReference.builder().build())
                    .build())
                .build()));

        when(additionalOffenceRepository.findByEventId(42L))
            .thenReturn(ImmutableList.of(
                AdditionalOffence.builder()
                    .additionalOffenceId(32L)
                    .softDeleted(0L)
                    .eventId(42L)
                    .offence(Offence.builder()
                        .ogrsOffenceCategory(StandardReference.builder().build())
                        .build())
                    .build(),
                AdditionalOffence.builder()
                    .additionalOffenceId(33L)
                    .softDeleted(0L)
                    .eventId(42L)
                    .offence(Offence.builder()
                        .ogrsOffenceCategory(StandardReference.builder().build())
                        .build())
                    .build()));
    }

    private InstitutionalReport anInstitutionalReportLinkedToAnEvent() {
        return InstitutionalReport.builder()
            .institutionalReportId(2L)
            .custody(Custody.builder()
                .disposal(Disposal.builder()
                    .event(Event.builder()
                        .eventId(42L)
                        .build())
                    .build())
                .build())
            .softDeleted(0L)
            .build();
    }

    @Test
    public void singleReportFilteredOutWhenSoftDeleted() {
        assertThat(institutionalReportService.institutionalReportFor(1L, 1L).isPresent()).isFalse();
        assertThat(institutionalReportService.institutionalReportFor(2L, 2L).isPresent()).isTrue();
    }

    @Test
    public void listOfReportsFiltersOutSoftDeleted() {
        assertThat(institutionalReportService.institutionalReportsFor(3L).size()).isEqualTo(2);
    }

    @Test
    public void convictionIsEmbellishedWithMainOffence() {

        uk.gov.justice.digital.delius.data.api.InstitutionalReport institutionalReport =
            institutionalReportService.institutionalReportFor(2L, 2L).get();

        assertThat(institutionalReport.getConviction().getOffences()).hasSize(3);
        assertThat(institutionalReport.getConviction().getOffences().get(0).getOffenceId()).isEqualTo("M22");
        assertThat(institutionalReport.getConviction().getOffences().get(1).getOffenceId()).isEqualTo("A32");
        assertThat(institutionalReport.getConviction().getOffences().get(2).getOffenceId()).isEqualTo("A33");
    }

    @Test
    public void sentenceIsEmbellishedWithOutcomeDescription() {

        uk.gov.justice.digital.delius.data.api.InstitutionalReport institutionalReport =
            institutionalReportService.institutionalReportFor(2L, 2L).get();

        assertThat(institutionalReport.getSentence().getDescription()).isEqualTo("Some sentence text");
    }

    @Test
    public void sentenceIsEmbellishedWithOutcomeDescriptionForListOfReports() {
        List<uk.gov.justice.digital.delius.data.api.InstitutionalReport> institutionalReports =
            institutionalReportService.institutionalReportsFor(3L);

        assertThat(institutionalReports.get(0).getSentence().getDescription()).isEqualTo("Some sentence text");
    }
}