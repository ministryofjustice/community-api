package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.jpa.standard.entity.InstitutionalReport;
import uk.gov.justice.digital.delius.jpa.standard.repository.InstitutionalReportRepository;
import uk.gov.justice.digital.delius.transformers.InstitutionalReportTransformer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import({InstitutionalReportService.class, InstitutionalReportTransformer.class})
public class InstitutionalReportServiceTest {

    @Autowired
    private InstitutionalReportService institutionalReportService;

    @MockBean
    private InstitutionalReportRepository institutionalReportRepository;

    @Before
    public void setup() {
        when(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(1L, 1L)).
            thenReturn(Optional.of(InstitutionalReport.builder()
                .softDeleted(1L)
                .build()));

        when(institutionalReportRepository.findByOffenderIdAndInstitutionalReportId(2L, 2L)).
            thenReturn(Optional.of(InstitutionalReport.builder()
                .softDeleted(0L)
                .build()));

        when(institutionalReportRepository.findByOffenderId(3L)).
            thenReturn(ImmutableList.of(
                InstitutionalReport.builder()
                    .softDeleted(1L)
                    .build(),
                InstitutionalReport.builder()
                    .softDeleted(0L)
                    .build(),
                InstitutionalReport.builder()
                    .softDeleted(0L)
                    .build())
            );
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
}