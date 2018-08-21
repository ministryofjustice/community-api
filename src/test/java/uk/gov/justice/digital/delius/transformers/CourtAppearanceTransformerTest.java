package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CourtAppearanceTransformerTest {

    private CourtAppearanceTransformer courtAppearanceTransformer = new CourtAppearanceTransformer();

    @Test
    public void itFiltersOutSoftDeletedEntries() {

        CourtAppearance courtAppearance = CourtAppearance.builder()
            .courtAppearanceId(1L)
            .appearanceDate(LocalDateTime.now())
            .court(aCourt())
            .courtReports(ImmutableList.of(
                uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                    .courtReportId(1L)
                    .build(),
                uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                    .courtReportId(2L)
                    .softDeleted(1L)
                    .build(),
                uk.gov.justice.digital.delius.jpa.standard.entity.CourtReport.builder()
                    .courtReportId(3L)
                    .build()
            ))
            .build();

        List<CourtReport> courtReports = courtAppearanceTransformer.courtAppearanceOf(courtAppearance).getCourtReports();
        assertThat(courtReports)
            .extracting("courtReportId").containsOnly(1L, 3L);
    }

    private Court aCourt() {
        return Court.builder()
            .courtId(1L)
            .build();
    }
}