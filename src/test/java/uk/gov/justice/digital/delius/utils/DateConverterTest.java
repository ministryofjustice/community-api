package uk.gov.justice.digital.delius.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DateConverterTest {

    private static final OffsetDateTime BEFORE_BST = OffsetDateTime.of(2021, 1, 31, 23, 30, 1, 0, ZoneOffset.UTC);
    private static final OffsetDateTime AFTER_BST = OffsetDateTime.of(2021, 3, 31, 23, 30, 1, 0, ZoneOffset.UTC);

    @Test
    public void europeLondonZoneExists() {
        assertThat(ZoneId.getAvailableZoneIds().contains("Europe/London")).isTrue();
    }

    @Test
    public void londonAndGMTAreSameBeforeBST() {
        assertThat(DateConverter.toLondonLocalDate(BEFORE_BST)).isEqualTo(LocalDate.of(2021, 1, 31));
        assertThat(DateConverter.toLondonLocalTime(BEFORE_BST)).isEqualTo(LocalTime.of(23, 30, 1));
        assertThat(DateConverter.toLondonLocalDateTime(BEFORE_BST)).isEqualTo(LocalDateTime.of(2021, 1, 31, 23, 30, 1));
    }

    @Test
    public void londonTimeAndGMTAreNotSameAfterBST() {
        assertThat(DateConverter.toLondonLocalDate(AFTER_BST)).isEqualTo(LocalDate.of(2021, 4, 1));
        assertThat(DateConverter.toLondonLocalTime(AFTER_BST)).isEqualTo(LocalTime.of(0, 30, 1));
        assertThat(DateConverter.toLondonLocalDateTime(AFTER_BST)).isEqualTo(LocalDateTime.of(2021, 4, 1, 0, 30, 1));
    }
}