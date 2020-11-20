package uk.gov.justice.digital.delius.data.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HumanTest {

    @Test
    @DisplayName("Will capitalise first letter in each name")
    void capitalise() {
        final var human = Human.builder().forenames("JOHN barry").surname("SmItH").build().capitalise();

        assertThat(human.getForenames()).isEqualTo("John Barry");
        assertThat(human.getSurname()).isEqualTo("Smith");
    }
}