package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalReportRepository;
import uk.gov.justice.digital.delius.transformers.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@Import({InstitutionalReportService.class, InstitutionalReportTransformer.class,
    MainOffenceTransformer.class, AdditionalOffenceTransformer.class, ConvictionTransformer.class, InstitutionTransformer.class})
public class InstitutionalReportServiceTest {

    public static final long EVENT_ID = 42L;
    @Autowired
    private InstitutionalReportService institutionalReportService;

    @MockBean
    private InstitutionalReportRepository institutionalReportRepository;

    @MockBean
    private LookupSupplier lookupSupplier;
    @MockBean
    private CourtAppearanceTransformer courtAppearanceTransformer;

    @Test
    public void singleReportFilteredOutWhenSoftDeleted() {
        given(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(1L, 2L)).
            willReturn(Optional.of(InstitutionalReport.builder().softDeleted(1L).build()));

        assertThat(institutionalReportService.institutionalReportFor(1L, 2L).isPresent()).isFalse();
    }

    @Test
    public void listOfReportsFiltersOutSoftDeleted() {
        given(institutionalReportRepository.findByOffenderId(1L)).
            willReturn(ImmutableList.of(
                InstitutionalReport.builder().softDeleted(0L).build(),
                InstitutionalReport.builder().softDeleted(1L).build(),
                InstitutionalReport.builder().softDeleted(0L).build())
            );

        assertThat(institutionalReportService.institutionalReportsFor(1L).size()).isEqualTo(2);
    }

    @Test
    public void convictionIsEmbellishedWithMainOffence() {

        given(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(1L, 2L)).
            willReturn(Optional.of(anInstitutionalReport(Event
                    .builder()
                    .eventId(EVENT_ID)
                    .mainOffence(MainOffence.builder()
                            .mainOffenceId(22L)
                            .offence(Offence.builder()
                                    .ogrsOffenceCategory(StandardReference.builder().build())
                                    .build())
                            .build())
                    .additionalOffences(ImmutableList.of(
                            AdditionalOffence.builder()
                                    .additionalOffenceId(32L)
                                    .event(Event.builder().eventId(EVENT_ID).build())
                                    .offence(Offence.builder()
                                            .ogrsOffenceCategory(StandardReference.builder().build())
                                            .build())
                                    .build(),
                            AdditionalOffence.builder()
                                    .additionalOffenceId(33L)
                                    .event(Event.builder().eventId(EVENT_ID).build())
                                    .offence(Offence.builder()
                                            .ogrsOffenceCategory(StandardReference.builder().build())
                                            .build())
                                    .build()))
                    .build())));

        uk.gov.justice.digital.delius.data.api.InstitutionalReport institutionalReport =
            institutionalReportService.institutionalReportFor(1L, 2L).get();

        assertThat(institutionalReport.getConviction().getOffences())
            .extracting("offenceId").containsOnly("M22", "A32", "A33");
    }

    @Test
    public void sentenceIsEmbellishedWithOutcomeDescription() {
        given(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(1L, 2L)).
            willReturn(Optional.of(anInstitutionalReportWithSentenceDescription("Some sentence text")));

        uk.gov.justice.digital.delius.data.api.InstitutionalReport institutionalReport =
            institutionalReportService.institutionalReportFor(1L, 2L).get();

        assertThat(institutionalReport.getSentence().getDescription()).isEqualTo("Some sentence text");
    }

    @Test
    public void sentenceIsEmbellishedWithOutcomeDescriptionForListOfReports() {
        given(institutionalReportRepository.findByOffenderId(1L)).
            willReturn(ImmutableList.of(
                InstitutionalReport.builder().softDeleted(1L).build(),
                anInstitutionalReportWithSentenceDescription("Some other sentence text"),
                InstitutionalReport.builder().softDeleted(0L).build())
            );

        List<uk.gov.justice.digital.delius.data.api.InstitutionalReport> institutionalReports =
            institutionalReportService.institutionalReportsFor(1L);

        assertThat(institutionalReports.get(0).getSentence().getDescription()).isEqualTo("Some other sentence text");
    }

    private InstitutionalReport anInstitutionalReport(Event event) {
        return InstitutionalReport.builder()
            .institutionalReportId(1L)
            .custody(Custody.builder()
                .disposal(Disposal.builder()
                    .event(event)
                    .build())
                .build())
            .build();
    }

    private InstitutionalReport anInstitutionalReportWithSentenceDescription(String sentenceDescription) {
        final Disposal disposal = Disposal.builder()
                .disposalType(DisposalType.builder()
                        .description(sentenceDescription)
                        .build())
                .event(Event
                        .builder()
                        .additionalOffences(ImmutableList.of())
                        .build())
                .build();

        final Event event = disposal.getEvent().toBuilder().disposal(disposal).build();

        return InstitutionalReport.builder()
            .institutionalReportId(2L)
            .custody(Custody.builder()
                .disposal(disposal.toBuilder().event(event).build()) // simulate back reference
                .build())
            .build();
    }
}