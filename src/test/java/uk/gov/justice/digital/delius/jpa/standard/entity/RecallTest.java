package uk.gov.justice.digital.delius.jpa.standard.entity;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RecallTest {

    @Test
    public void isSoftDeleted_no_returnsFalse() {
        final var recall = Recall.builder().softDeleted(0L).build();

        assertThat(recall.isSoftDeleted()).isFalse();
    }

    @Test
    public void isSoftDeleted_yes_returnsTrue() {
        final var recall = Recall.builder().softDeleted(1L).build();

        assertThat(recall.isSoftDeleted()).isTrue();
    }
}
