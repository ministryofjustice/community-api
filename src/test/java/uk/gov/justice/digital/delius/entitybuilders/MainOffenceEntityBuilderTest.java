package uk.gov.justice.digital.delius.entitybuilders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MainOffenceEntityBuilderTest {
    @Mock
    private LookupSupplier lookupSupplier;

    private MainOffenceEntityBuilder mainOffenceEntityBuilder;

    @BeforeEach
    public void setup() {
        mainOffenceEntityBuilder = new MainOffenceEntityBuilder(lookupSupplier);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.offenceSupplier()).thenReturn(code -> Offence.builder().code(code).build());

    }

    @Test
    public void setsSensibleDefaults() {
        final MainOffence mainOffence = mainOffenceEntityBuilder.mainOffenceOf(1L, anOffence(), anEvent());

        assertThat(mainOffence.getPartitionAreaId()).isEqualTo(0L);
        assertThat(mainOffence.getRowVersion()).isEqualTo(1L);
        assertThat(mainOffence.getSoftDeleted()).isEqualTo(0L);
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());

        final MainOffence mainOffence = mainOffenceEntityBuilder.mainOffenceOf(1L, anOffence(), anEvent());

        assertThat(mainOffence.getCreatedByUserId()).isEqualTo(99L);
        assertThat(mainOffence.getLastUpdatedUserId()).isEqualTo(99L);
        assertThat(mainOffence.getCreatedDatetime()).isNotNull();
        assertThat(mainOffence.getLastUpdatedDatetime()).isNotNull();
    }

    @Test
    public void offenceIsSetFromDetailCode() {
        final MainOffence mainOffence = mainOffenceEntityBuilder
                .mainOffenceOf(1L, anOffence().toBuilder().detail(anOffence().getDetail().toBuilder().code("AA").build()).build(), anEvent());

        assertThat(mainOffence.getOffence().getCode()).isEqualTo("AA");
    }


    private uk.gov.justice.digital.delius.data.api.Offence anOffence() {
        return uk.gov.justice.digital.delius.data.api.Offence.builder().detail(OffenceDetail.builder().build()).build();
    }

    private Event anEvent() {
        return Event
                .builder()
                .build();
    }

}
