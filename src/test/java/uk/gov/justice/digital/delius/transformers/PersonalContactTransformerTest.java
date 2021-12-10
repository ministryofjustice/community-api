package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.util.EntityHelper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;

public class PersonalContactTransformerTest {
    @Test
    public void transformsPersonalContact() {
        final var source = EntityHelper.aPersonalContact();
        final var observed = PersonalContactTransformer.personalContactOf(source);

        assertThat(observed)
            .usingRecursiveComparison()
            .ignoringFields("gender", "relationshipType", "title", "address.town", "isActive")
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

    @Test
    public void isActiveIsTrueWhenStartDateTodayAndEndDateInFuture() {
        final var today = LocalDateTime.now();
        final var source = EntityHelper.aPersonalContact(today, today.plusDays(1));
        final var observed = PersonalContactTransformer.personalContactOf(source);
        assertThat(observed)
            .hasFieldOrPropertyWithValue("startDate", today)
            .hasFieldOrPropertyWithValue("endDate", today.plusDays(1))
            .hasFieldOrPropertyWithValue("isActive", true);
    }

    @Test
    public void isActiveIsTrueWhenEndDateIsNull() {
        final var today = LocalDateTime.now();
        final var source = EntityHelper.aPersonalContact(today.minusDays(1), null);
        final var observed = PersonalContactTransformer.personalContactOf(source);
        assertThat(observed)
            .hasFieldOrPropertyWithValue("startDate", today.minusDays(1))
            .hasFieldOrPropertyWithValue("endDate", null)
            .hasFieldOrPropertyWithValue("isActive", true);
    }

    @Test
    public void isActiveIsFalseWhenEndDateIsToday() {
        final var today = LocalDateTime.now();

        final var source = EntityHelper.aPersonalContact(today.minusDays(1), today);
        final var observed = PersonalContactTransformer.personalContactOf(source);
        assertThat(observed)
            .hasFieldOrPropertyWithValue("startDate", today.minusDays(1))
            .hasFieldOrPropertyWithValue("endDate", today)
            .hasFieldOrPropertyWithValue("isActive", false);
    }

    @Test
    public void isActiveIsFalseWhenStartDateInFuture() {
        final var today = LocalDateTime.now();

        final var source = EntityHelper.aPersonalContact(today.plusDays(1), today.plusDays(2));
        final var observed = PersonalContactTransformer.personalContactOf(source);
        assertThat(observed)
            .hasFieldOrPropertyWithValue("startDate", today.plusDays(1))
            .hasFieldOrPropertyWithValue("endDate", today.plusDays(2))
            .hasFieldOrPropertyWithValue("isActive", false);
    }
}
