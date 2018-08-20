package uk.gov.justice.digital.delius.transformers;

import org.junit.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import static org.assertj.core.api.Assertions.assertThat;

public class MainOffenceTransformerTest {

    private MainOffenceTransformer mainOffenceTransformer = new MainOffenceTransformer();

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

}