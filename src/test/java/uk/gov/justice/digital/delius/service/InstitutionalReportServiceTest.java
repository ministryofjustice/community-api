package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.DisposalType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalReportRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class InstitutionalReportServiceTest {

    public static final long EVENT_ID = 42L;
    private InstitutionalReportService institutionalReportService;

    @Mock
    private InstitutionalReportRepository institutionalReportRepository;

    @BeforeEach
    void setUp() {
        institutionalReportService = new InstitutionalReportService(institutionalReportRepository);
    }

    @Test
    public void singleReportFilteredOutWhenSoftDeleted() {
        given(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(1L, 2L)).
            willReturn(Optional.of(InstitutionalReport.builder().softDeleted(1L).build()));

        assertThat(institutionalReportService.institutionalReportFor(1L, 2L).isPresent()).isFalse();
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
                    .additionalOffences(List.of(
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
                        .additionalOffences(List.of())
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