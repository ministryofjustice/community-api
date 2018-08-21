package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import static org.assertj.core.api.Assertions.assertThat;

public class AdditionalOffenceTransformerTest {

    private AdditionalOffenceTransformer  additionalOffenceTransformer = new AdditionalOffenceTransformer();

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

}