package uk.gov.justice.digital.delius.transformers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MainOffenceTransformerTest {
    @Mock
    private LookupSupplier lookupSupplier;

    private MainOffenceTransformer mainOffenceTransformer;

    @Before
    public void setup() {
        mainOffenceTransformer = new MainOffenceTransformer(lookupSupplier);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.offenceSupplier()).thenReturn(code -> Offence.builder().code(code).build());

    }

    @Test
    public void itConvertsTheIdCorrectly() {
        MainOffence mainOffence = MainOffence.builder()
            .mainOffenceId(92L)
            .offence(Offence.builder()
                .ogrsOffenceCategory(StandardReference.builder().build())
                .build())
            .build();

        assertThat(mainOffenceTransformer.offenceOf(mainOffence).getOffenceId()).isEqualTo("M92");
    }

    @Test
    public void itSetsTheMainOffenceFlagToTrue() {
        MainOffence mainOffence = MainOffence.builder()
            .offence(Offence.builder()
                .ogrsOffenceCategory(StandardReference.builder().build())
                .build())
            .build();

        assertThat(mainOffenceTransformer.offenceOf(mainOffence).getMainOffence()).isTrue();
    }

    @Test
    public void setsSensibleDefaults() {
        final MainOffence mainOffence = mainOffenceTransformer.mainOffenceOf(1L, anOffence(), anEvent());

        assertThat(mainOffence.getPartitionAreaId()).isEqualTo(0L);
        assertThat(mainOffence.getRowVersion()).isEqualTo(1L);
        assertThat(mainOffence.getSoftDeleted()).isEqualTo(0L);
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());

        final MainOffence mainOffence = mainOffenceTransformer.mainOffenceOf(1L, anOffence(), anEvent());

        assertThat(mainOffence.getCreatedByUserId()).isEqualTo(99L);
        assertThat(mainOffence.getLastUpdatedUserId()).isEqualTo(99L);
        assertThat(mainOffence.getCreatedDatetime()).isNotNull();
        assertThat(mainOffence.getLastUpdatedDatetime()).isNotNull();
    }

    @Test
    public void offenceIsSetFromDetailCode() {
        final MainOffence mainOffence = mainOffenceTransformer.mainOffenceOf(1L, anOffence().toBuilder().detail(anOffence().getDetail().toBuilder().code("AA").build()).build(), anEvent());

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
