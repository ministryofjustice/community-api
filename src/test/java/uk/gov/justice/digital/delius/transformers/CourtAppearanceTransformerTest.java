package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


public class CourtAppearanceTransformerTest {

    private CourtAppearanceTransformer courtAppearanceTransformer = new CourtAppearanceTransformer();

    @Test
    public void itFiltersOutSoftDeletedEntries() {

        ImmutableList<CourtAppearance> courtAppearances = ImmutableList.of(
            CourtAppearance.builder()
                .courtAppearanceId(1L)
                .appearanceDate(LocalDateTime.now())
                .court(aCourt())
                .courtReports(ImmutableList.of())
                .build(),
            CourtAppearance.builder()
                .courtAppearanceId(2L)
                .appearanceDate(LocalDateTime.now())
                .court(aCourt())
                .courtReports(ImmutableList.of())
                .softDeleted(1L)
                .build(),
            CourtAppearance.builder()
                .courtAppearanceId(3L)
                .appearanceDate(LocalDateTime.now())
                .court(aCourt())
                .courtReports(ImmutableList.of())
                .build());

        assertThat(courtAppearanceTransformer.courtAppearancesOf(courtAppearances))
            .extracting("courtAppearanceId").containsOnly(1L, 3L);
    }

    private Court aCourt() {
        return Court.builder()
            .courtId(1L)
            .build();
    }
}