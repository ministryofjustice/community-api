package uk.gov.justice.digital.delius.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateConverter {

    private static final String LOCAL_TIMEZONE = "Europe/London";

    public static LocalDate toLondonLocalDate(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.of(LOCAL_TIMEZONE)).toLocalDate();
    }

    public static LocalDateTime toLondonLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.of(LOCAL_TIMEZONE)).toLocalDateTime();
    }

    public static LocalTime toLondonLocalTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.of(LOCAL_TIMEZONE)).toLocalTime();
    }
}
