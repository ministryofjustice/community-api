package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtReportRepository;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;
import uk.gov.justice.digital.delius.transformers.CourtReportTransformer;
import uk.gov.justice.digital.delius.transformers.CourtTransformer;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import({CourtReportService.class, CourtReportTransformer.class, CourtAppearanceTransformer.class, CourtTransformer.class})
public class CourtReportServiceTest {
    @Autowired
    private CourtReportService courtReportService;

    @MockBean
    private CourtReportRepository courtReportRepository;

    @MockBean
    private LookupSupplier lookupSupplier;



    @Before
    public void before() {
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