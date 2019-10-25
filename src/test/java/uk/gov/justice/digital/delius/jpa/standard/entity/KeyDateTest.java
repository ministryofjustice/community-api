package uk.gov.justice.digital.delius.jpa.standard.entity;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyDateTest {

    @Test
    public void isSentenceExpiryWhenKeyDateIsSED() {
        assertThat(KeyDate.isSentenceExpiryKeyDate("SED")).isTrue();
    }

    @Test
    public void isNotSentenceExpiryWhenKeyDateIsPOM() {
        assertThat(KeyDate.isSentenceExpiryKeyDate("POM1")).isFalse();
    }
    @Test
    public void isNotSentenceExpiryWhenKeyDateIsNULL() {
        assertThat(KeyDate.isSentenceExpiryKeyDate(null)).isFalse();
    }
}