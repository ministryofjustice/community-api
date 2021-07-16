package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.RCourtReportType;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourtReportServiceTest {
    private CourtReportService courtReportService;

    @Mock
    private CourtReportRepository courtReportRepository;

    @BeforeEach
    void before() {
        courtReportService = new CourtReportService(courtReportRepository);
        when(courtReportRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                CourtReport.builder().courtReportId(1L).offenderId(1L).dateRequested(LocalDateTime.now().minusDays(98)).reportManagers(emptyList()).build(),
                CourtReport.builder().courtReportId(2L).offenderId(1L).dateRequested(LocalDateTime.now().minusDays(1)).reportManagers(emptyList()).build(),
                CourtReport.builder().courtReportId(3L).offenderId(1L).dateRequested(LocalDateTime.now()).softDeleted(true).reportManagers(emptyList()).build(),
                CourtReport.builder().courtReportId(4L).offenderId(1L).dateRequested(LocalDateTime.now().minusDays(99)).reportManagers(emptyList()).build()
        ));
        when(courtReportRepository.findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(any(), any())).thenReturn(
                Optional.of(CourtReport.builder().courtReportId(4L).offenderId(1L).reportManagers(emptyList()).build()));
    }

    @Test
    void courtReportsForFiltersDeletedRecords() {
        assertThat(courtReportService.courtReportsFor(1L)).hasSize(3);
    }

    @Test
    void courtReportsForOrdersByDateRequested() {
        assertThat(courtReportService.courtReportsFor(1L).get(0).getCourtReportId()).isEqualTo(2L);
        assertThat(courtReportService.courtReportsFor(1L).get(1).getCourtReportId()).isEqualTo(1L);
        assertThat(courtReportService.courtReportsFor(1L).get(2).getCourtReportId()).isEqualTo(4L);
    }

    @Test
    void courtReportForUsesBothOffenderIdAndCourtIdForLookup() {
        courtReportService.courtReportFor(1L, 4L);

        verify(courtReportRepository).findByOffenderIdAndCourtReportIdAndSoftDeletedFalse(1L, 4L);
    }

    @Test
    void minimalCourtReport() {
        assertThat(courtReportService.courtReportMinimalFor(1L, 0L).get().getCourtReportId()).isEqualTo(4);
    }

    @Test
    void givenNoReports_whenGetReportsForOffenderAndEventId_thenReturnEmptyList() {

        when(courtReportRepository.findByOffenderIdAndEventId(1L, 99L)).thenReturn(emptyList());

        assertThat(courtReportService.courtReportsMinimalFor(1L, 99L)).isEmpty();
    }

    @Test
    void givenMultipleReports_whenGetReportsForOffenderAndEventId_thenReturn() {

        var report1 = CourtReport.builder()
            .dateRequested(LocalDateTime.now())
            .courtReportType(RCourtReportType.builder().code("CJF").description("PSR - Fast").build())
            .reportManagers(List.of(EntityHelper.aReportManager(true)))
            .build();
        var report2 = CourtReport.builder()
            .dateRequested(LocalDateTime.now())
            .courtReportType(RCourtReportType.builder().code("XXX").description("Some other report").build())
            .reportManagers(emptyList())
            .build();
        when(courtReportRepository.findByOffenderIdAndEventId(1L, 99L)).thenReturn(List.of(report1, report2));

        var courtReports= courtReportService.courtReportsMinimalFor(1L, 99L);
        assertThat(courtReports).extracting("courtReportType.code").containsExactly("CJF", "XXX");
    }
}
