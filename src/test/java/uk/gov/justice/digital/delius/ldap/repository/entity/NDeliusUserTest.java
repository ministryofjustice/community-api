package uk.gov.justice.digital.delius.ldap.repository.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class NDeliusUserTest {

    @Test
    void isEnabled_endDateInPast() {
        final var nDeliusUser = NDeliusUser.builder().endDate("20190102000000Z").build();

        assertThat(nDeliusUser.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_endDateInFuture() {
        final var tomorrow = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now().plusDays(1));
        final var nDeliusUser = NDeliusUser.builder().endDate(tomorrow + "000000Z").build();

        assertThat(nDeliusUser.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_null() {
        final var nDeliusUser = new NDeliusUser();
        assertThat(nDeliusUser.isEnabled()).isTrue();
    }
}
