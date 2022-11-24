package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponsibleOfficerTest {

    @Test
    void canMakeInactive() {
        final var responsibleOfficer = ResponsibleOfficer.builder().endDateTime(null).build();
        assertThat(responsibleOfficer.isActive()).isTrue();
        responsibleOfficer.makeInactive();
        assertThat(responsibleOfficer.isActive()).isFalse();
        assertThat(responsibleOfficer.getEndDateTime()).isNotNull();
    }

}