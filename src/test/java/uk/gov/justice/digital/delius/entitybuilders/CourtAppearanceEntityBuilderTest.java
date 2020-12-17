package uk.gov.justice.digital.delius.entitybuilders;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CourtAppearanceEntityBuilderTest {
    @Mock
    private LookupSupplier lookupSupplier;

    private CourtAppearanceEntityBuilder courtAppearanceEntityBuilder;

    @BeforeEach
    public void setup() {
        courtAppearanceEntityBuilder = new CourtAppearanceEntityBuilder(lookupSupplier);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.courtSupplier()).thenReturn(courtId -> Court.builder().courtId(courtId).build());
    }

    @Test
    public void setsSensibleDefaults() {
        final CourtAppearance courtAppearance = courtAppearanceEntityBuilder.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance());

        assertThat(courtAppearance.getRowVersion()).isEqualTo(1L);
        assertThat(courtAppearance.getPartitionAreaId()).isEqualTo(0L);
        assertThat(courtAppearance.getSoftDeleted()).isEqualTo(0L);
    }

    @Test
    public void outcomeNotMappedIfNotPresent() {
        final CourtAppearance courtAppearance = courtAppearanceEntityBuilder.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance().toBuilder().outcome(null).build());

        assertThat(courtAppearance.getOutcome()).isNull();
    }

    @Test
    public void outcomeIsMappedWhenPresent() {
        when(lookupSupplier.courtAppearanceOutcomeSupplier()).thenReturn(code -> StandardReference.builder().codeValue(code).build());
        final CourtAppearance courtAppearance = courtAppearanceEntityBuilder.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance().toBuilder().outcome(KeyValue.builder().code("AA").build()).build());

        assertThat(courtAppearance.getOutcome()).isNotNull();
        assertThat(courtAppearance.getOutcome().getCodeValue()).isEqualTo("AA");
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.courtAppearanceOutcomeSupplier()).thenReturn(code -> StandardReference.builder().codeValue(code).build());
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());

        final CourtAppearance courtAppearance = courtAppearanceEntityBuilder.courtAppearanceOf(1L, aEvent(), aApiCourtAppearance().toBuilder().outcome(KeyValue.builder().code("AA").build()).build());

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

}
