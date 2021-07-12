package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.util.EntityHelper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PersonalContactTransformerTest {
    @Test
    public void transformsPersonalContact() {
        final var source = EntityHelper.aPersonalContact();
        final var observed = PersonalContactTransformer.personalContactOf(source);

        assertThat(observed)
            .usingRecursiveComparison()
            .ignoringFields("gender", "relationshipType", "title", "address.town")
            .isEqualTo(source);

        assertThat(observed)
            .hasFieldOrPropertyWithValue("gender", source.getGender().getCodeDescription())
            .hasFieldOrPropertyWithValue("relationshipType.code", source.getRelationshipType().getCodeValue())
            .hasFieldOrPropertyWithValue("relationshipType.description", source.getRelationshipType().getCodeDescription())
            .hasFieldOrPropertyWithValue("title", source.getTitle().getCodeDescription())
            .hasFieldOrPropertyWithValue("address.town", source.getAddress().getTownCity());
    }

    @Test
    public void transformsPersonalContactWithoutAddress() {
        final var source = EntityHelper.aPersonalContact().toBuilder().address(null).build();
        final var observed = PersonalContactTransformer.personalContactOf(source);

        assertThat(observed.getAddress()).isNull();
    }
}
