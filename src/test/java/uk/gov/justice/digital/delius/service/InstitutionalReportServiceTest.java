package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.DisposalType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.AdditionalOffenceRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalReportRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;
import uk.gov.justice.digital.delius.transformers.AdditionalOffenceTransformer;
import uk.gov.justice.digital.delius.transformers.InstitutionalReportTransformer;
import uk.gov.justice.digital.delius.transformers.MainOffenceTransformer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import({InstitutionalReportService.class, InstitutionalReportTransformer.class,
    MainOffenceTransformer.class, AdditionalOffenceTransformer.class, OffenceService.class})
public class InstitutionalReportServiceTest {

    public static final long EVENT_ID = 42L;
    @Autowired
    private InstitutionalReportService institutionalReportService;

    @MockBean
    private InstitutionalReportRepository institutionalReportRepository;

    @MockBean
    private MainOffenceRepository mainOffenceRepository;

    @MockBean
    private AdditionalOffenceRepository additionalOffenceRepository;

    private InstitutionalReport anInstitutionalReportWithSentenceDescription(String sentenceDescription) {
        return InstitutionalReport.builder()
            .institutionalReportId(2L)
            .custody(Custody.builder()
                .disposal(Disposal.builder()
                    .disposalType(DisposalType.builder()
                        .description(sentenceDescription)
                        .build())
                    .build())
                .build())
            .build();
    }

    private InstitutionalReport anInstitutionalReport() {
        return InstitutionalReport.builder()
            .institutionalReportId(1L)
            .custody(Custody.builder()
                .disposal(Disposal.builder()
                    .event(Event.builder()
                        .eventId(EVENT_ID)
                        .build())
                    .build())
                .build())
            .build();
    }

    @Test
    public void singleReportFilteredOutWhenSoftDeleted() {
        given(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(1L, 1L)).
            willReturn(Optional.of(InstitutionalReport.builder().softDeleted(1L).build()));

        assertThat(institutionalReportService.institutionalReportFor(1L, 1L).isPresent()).isFalse();
    }

    @Test
    public void listOfReportsFiltersOutSoftDeleted() {
        given(institutionalReportRepository.findByOffenderId(3L)).
            willReturn(ImmutableList.of(
                InstitutionalReport.builder().softDeleted(0L).build(),
                InstitutionalReport.builder().softDeleted(1L).build(),
                InstitutionalReport.builder().softDeleted(0L).build())
            );

        assertThat(institutionalReportService.institutionalReportsFor(3L).size()).isEqualTo(2);
    }

    @Test
    public void convictionIsEmbellishedWithMainOffence() {
        when(mainOffenceRepository.findByEventId(EVENT_ID))
            .thenReturn(ImmutableList.of(MainOffence.builder()
                .mainOffenceId(22L)
                .eventId(EVENT_ID)
                .offence(Offence.builder()
                    .ogrsOffenceCategory(StandardReference.builder().build())
                    .build())
                .build()));


        when(additionalOffenceRepository.findByEventId(EVENT_ID))
            .thenReturn(ImmutableList.of(
                AdditionalOffence.builder()
                    .additionalOffenceId(32L)
                    .eventId(EVENT_ID)
                    .offence(Offence.builder()
                        .ogrsOffenceCategory(StandardReference.builder().build())
                        .build())
                    .build(),
                AdditionalOffence.builder()
                    .additionalOffenceId(33L)
                    .eventId(EVENT_ID)
                    .offence(Offence.builder()
                        .ogrsOffenceCategory(StandardReference.builder().build())
                        .build())
                    .build()));

        given(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(2L, 2L)).
            willReturn(Optional.of(anInstitutionalReport()));

        uk.gov.justice.digital.delius.data.api.InstitutionalReport institutionalReport =
            institutionalReportService.institutionalReportFor(2L, 2L).get();

        assertThat(institutionalReport.getConviction().getOffences()).hasSize(3);
        assertThat(institutionalReport.getConviction().getOffences().get(0).getOffenceId()).isEqualTo("M22");
        assertThat(institutionalReport.getConviction().getOffences().get(1).getOffenceId()).isEqualTo("A32");
        assertThat(institutionalReport.getConviction().getOffences().get(2).getOffenceId()).isEqualTo("A33");
    }

    @Test
    public void sentenceIsEmbellishedWithOutcomeDescription() {
        given(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(2L, 2L)).
            willReturn(Optional.of(anInstitutionalReportWithSentenceDescription("Some sentence text")));

        uk.gov.justice.digital.delius.data.api.InstitutionalReport institutionalReport =
            institutionalReportService.institutionalReportFor(2L, 2L).get();

        assertThat(institutionalReport.getSentence().getDescription()).isEqualTo("Some sentence text");
    }

    @Test
    public void sentenceIsEmbellishedWithOutcomeDescriptionForListOfReports() {
        given(institutionalReportRepository.findByOffenderId(3L)).
            willReturn(ImmutableList.of(
                InstitutionalReport.builder().softDeleted(1L).build(),
                anInstitutionalReportWithSentenceDescription("Some other sentence text"),
                InstitutionalReport.builder().softDeleted(0L).build())
            );

        List<uk.gov.justice.digital.delius.data.api.InstitutionalReport> institutionalReports =
            institutionalReportService.institutionalReportsFor(3L);

        assertThat(institutionalReports.get(0).getSentence().getDescription()).isEqualTo("Some other sentence text");
    }
}