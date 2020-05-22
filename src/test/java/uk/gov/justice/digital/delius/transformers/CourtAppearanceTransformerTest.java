package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.CourtReport;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CourtAppearanceTransformerTest {
    @Mock
    private LookupSupplier lookupSupplier;

    private CourtAppearanceTransformer courtAppearanceTransformer;

    @Before
    public void setup() {
        courtAppearanceTransformer = new CourtAppearanceTransformer(new CourtReportTransformer(new CourtTransformer()), new CourtTransformer(), lookupSupplier);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.courtAppearanceOutcomeSupplier()).thenReturn(code -> StandardReference.builder().codeValue(code).build());
        when(lookupSupplier.courtSupplier()).thenReturn(courtId -> Court.builder().courtId(courtId).build());

    }
    @Test
    public void itFiltersOutSoftDeletedEntries() {

        CourtAppearance courtAppearance = CourtAppearance.builder()
            .courtAppearanceId(1L)
            .appearanceDate(LocalDateTime.now())
            .court(aCourt())
            .event(aEvent())
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

        List<CourtReport> courtReports = CourtAppearanceTransformer
                .courtAppearanceOf(courtAppearance).getCourtReports();
        assertThat(courtReports)
            .extracting("courtReportId").containsOnly(1L, 3L);
    }

    @Test
    public void setsSensibleDefaults() {
        final uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance = courtAppearanceTransformer.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance());

        assertThat(courtAppearance.getRowVersion()).isEqualTo(1L);
        assertThat(courtAppearance.getPartitionAreaId()).isEqualTo(0L);
        assertThat(courtAppearance.getSoftDeleted()).isEqualTo(0L);
    }

    @Test
    public void outcomeNotMappedIfNotPresent() {
        final uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance = courtAppearanceTransformer.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance().toBuilder().outcome(null).build());

        assertThat(courtAppearance.getOutcome()).isNull();
    }

    @Test
    public void outcomeIsMappedWhenPresent() {
        final uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance = courtAppearanceTransformer.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance().toBuilder().outcome(KeyValue.builder().code("AA").build()).build());

        assertThat(courtAppearance.getOutcome()).isNotNull();
        assertThat(courtAppearance.getOutcome().getCodeValue()).isEqualTo("AA");
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());

        final uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance courtAppearance = courtAppearanceTransformer.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance().toBuilder().outcome(KeyValue.builder().code("AA").build()).build());

        assertThat(courtAppearance.getCreatedByUserId()).isEqualTo(99L);
        assertThat(courtAppearance.getLastUpdatedUserId()).isEqualTo(99L);
        assertThat(courtAppearance.getCreatedDatetime()).isNotNull();
        assertThat(courtAppearance.getLastUpdatedDatetime()).isNotNull();
    }


    private uk.gov.justice.digital.delius.data.api.CourtAppearance aApiCourtAppearance() {
        return uk.gov.justice.digital.delius.data.api.CourtAppearance
                .builder()
                .court(aApiCourt())
                .build();
    }

    private uk.gov.justice.digital.delius.data.api.Court aApiCourt() {
        return uk.gov.justice.digital.delius.data.api.Court.builder().build();
    }

    private Event aEvent() {
        return Event
                .builder()
                .additionalOffences(ImmutableList.of())
                .build();
    }

    private Court aCourt() {
        return Court.builder()
            .courtId(1L)
            .build();
    }
}
