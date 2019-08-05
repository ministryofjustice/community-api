package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdditionalOffenceTransformerTest {
    @Mock
    private LookupSupplier lookupSupplier;

    private AdditionalOffenceTransformer  additionalOffenceTransformer;

    @Before
    public void setup() {
        additionalOffenceTransformer = new AdditionalOffenceTransformer(lookupSupplier);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.offenceSupplier()).thenReturn((code) -> Offence.builder().offenceId(88L).build());
    }
    @Test
    public void itFiltersOutSoftDeletedEntries() {

        ImmutableList<AdditionalOffence> additionalOffences = ImmutableList.of(
            AdditionalOffence.builder()
                .additionalOffenceId(1L)
                .offence(Offence.builder()
                    .ogrsOffenceCategory(StandardReference.builder().build())
                    .build())
                .build(),
            AdditionalOffence.builder()
                .additionalOffenceId(2L)
                .offence(Offence.builder()
                    .ogrsOffenceCategory(StandardReference.builder().build())
                    .build())
                .softDeleted(1L)
                .build(),
            AdditionalOffence.builder()
                .additionalOffenceId(3L)
                .offence(Offence.builder()
                    .ogrsOffenceCategory(StandardReference.builder().build())
                    .build())
                .build()
        );

        assertThat(additionalOffenceTransformer.offencesOf(additionalOffences))
            .extracting("offenceId").containsOnly("A1", "A3");
    }


    @Test
    public void itConvertsTheIdCorrectly() {
        ImmutableList<AdditionalOffence> additionalOffences = ImmutableList.of(
            AdditionalOffence.builder()
                .additionalOffenceId(92L)
                .offence(Offence.builder()
                    .ogrsOffenceCategory(StandardReference.builder().build())
                    .build())
                .build()
        );

        assertThat(additionalOffenceTransformer.offencesOf(additionalOffences).get(0).getOffenceId())
            .isEqualTo("A92");
    }

    @Test
    public void itSetsTheMainOffenceFlagToFalse() {
        ImmutableList<AdditionalOffence> additionalOffences = ImmutableList.of(
            AdditionalOffence.builder()
                .offence(Offence.builder()
                    .ogrsOffenceCategory(StandardReference.builder().build())
                    .build())
                .build()
        );

        assertThat(additionalOffenceTransformer.offencesOf(additionalOffences).get(0).getMainOffence()).isFalse();
    }


    @Test
    public void eachOffenceIsCopied() {
        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence(),
                anOffence()
        );

        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent())).hasSize(2);
    }

    @Test
    public void setsAuditFields() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());

        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence()
        );

        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getCreatedByUserId()).isEqualTo(99L);
        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getLastUpdatedUserId()).isEqualTo(99L);
        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getCreatedDatetime()).isNotNull();
        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getLastUpdatedDatetime()).isNotNull();
    }

    @Test
    public void setsSensibleDefaults() {
        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence()
        );

        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getSoftDeleted()).isEqualTo(0L);
        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getPartitionAreaId()).isEqualTo(0L);
        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getRowVersion()).isEqualTo(1L);

    }

    @Test
    public void offenceDetailLookedUpFromCode() {
        when(lookupSupplier.offenceSupplier()).thenReturn(code -> Offence.builder().offenceId(88L).build());

        ImmutableList<uk.gov.justice.digital.delius.data.api.Offence> offences = ImmutableList.of(
                anOffence()
                        .toBuilder()
                        .detail(OffenceDetail
                                .builder()
                                .code("ABC")
                                .build())
                        .build()
        );

        assertThat(additionalOffenceTransformer.additionalOffencesOf(offences, anEvent()).get(0).getOffence().getOffenceId()).isEqualTo(88L);

    }
    private Event anEvent() {
        return Event.builder().build();
    }

    private uk.gov.justice.digital.delius.data.api.Offence anOffence() {
        return uk.gov.justice.digital.delius.data.api.Offence
                .builder()
                .detail(OffenceDetail
                        .builder()
                        .build())
                .build();
    }

}