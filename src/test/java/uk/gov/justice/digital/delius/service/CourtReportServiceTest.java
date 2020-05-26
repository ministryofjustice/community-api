package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CourtReportServiceTest {
    private CourtReportService courtReportService;

    @Mock
    private CourtReportRepository courtReportRepository;


    @BeforeEach
    public void before() {
        courtReportService = new CourtReportService(courtReportRepository);
        when(courtReportRepository.findByOffenderId(any())).thenReturn(ImmutableList.of(
                uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder().courtReportId(1L).offenderId(1L).dateRequested(LocalDateTime.now().minusDays(98)).build(),
                uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder().courtReportId(2L).offenderId(1L).dateRequested(LocalDateTime.now().minusDays(1)).build(),
                uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder().courtReportId(3L).offenderId(1L).dateRequested(LocalDateTime.now()).softDeleted(1L).build(),
                uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder().courtReportId(4L).offenderId(1L).dateRequested(LocalDateTime.now().minusDays(99)).build()
        ));
        when(courtReportRepository.findByOffenderIdAndCourtReportId(any(), any())).thenReturn(
                Optional.of(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder().courtReportId(4L).offenderId(1L).build()));

    }

    @Test
    public void courtReportsForFiltersDeletedRecords() {
        assertThat(courtReportService.courtReportsFor(1L)).hasSize(3);
    }

    @Test
    public void courtReportsForOrdersByDateRequested() {
        assertThat(courtReportService.courtReportsFor(1L).get(0).getCourtReportId()).isEqualTo(2L);
        assertThat(courtReportService.courtReportsFor(1L).get(1).getCourtReportId()).isEqualTo(1L);
        assertThat(courtReportService.courtReportsFor(1L).get(2).getCourtReportId()).isEqualTo(4L);
    }

    @Test
    public void courtReportForFiltersRecords() {
        when(courtReportRepository.findByOffenderIdAndCourtReportId(any(), any())).thenReturn(
                Optional.of(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder().courtReportId(4L).offenderId(1L).softDeleted(1L).build()));

        assertThat(courtReportService.courtReportFor(1L, 4L).isPresent()).isFalse();

        when(courtReportRepository.findByOffenderIdAndCourtReportId(any(), any())).thenReturn(
                Optional.of(uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder().courtReportId(4L).offenderId(1L).softDeleted(0L).build()));

        assertThat(courtReportService.courtReportFor(1L, 4L).isPresent()).isTrue();
    }

    @Test
    public void courtReportForUsesBothOffenderIdAndCourtIdForLookup() {
        courtReportService.courtReportFor(1L, 4L);

        verify(courtReportRepository).findByOffenderIdAndCourtReportId(1L, 4L);
    }

}
