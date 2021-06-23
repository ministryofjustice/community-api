package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CourtAppearanceBasic;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class CourtAppearanceBasicTransformerTest {

    public static final LocalDateTime APPEARANCE_DATE = LocalDateTime.of(1975, 9, 6, 20, 45);
    public static final String COURT_CODE = "C2";
    public static final String COURT_NAME = "Somewhere Magistrates' Court";
    public static final String APPEARANCE_TYPE_DESCRIPTION = "codeDescription";
    public static final String APPEARANCE_TYPE_CODE = "codeDescription";

    @Test
    public void populatesDto() {
        final var courtAppearance = aCourtAppearance(APPEARANCE_DATE, APPEARANCE_TYPE_CODE);

        final var observed = CourtAppearanceBasicTransformer.courtAppearanceOf(courtAppearance);

        shouldBeCourtAppearanceBasic(observed, APPEARANCE_DATE, APPEARANCE_TYPE_CODE);
    }

    @Test
    public void getsSentencingAppearance() {
        final var sentencingDate = LocalDateTime.of(2005, 6, 10, 10, 0);
        final var otherAppearance = aCourtAppearance(LocalDateTime.of(2005, 6, 11, 10, 0), APPEARANCE_TYPE_CODE);
        final var sentencingAppearance = aCourtAppearance(sentencingDate, "S");

        final var observed = CourtAppearanceBasicTransformer.latestOrSentencingCourtAppearanceOf(List.of(otherAppearance, sentencingAppearance));

        shouldBeCourtAppearanceBasic(observed, sentencingDate, "S");
    }

    @Test
    public void getsLatestAppearanceWhenNoSentencingAppearance() {
        final var latestDate = LocalDateTime.of(2005, 6, 12, 10, 0);
        final var appearance = aCourtAppearance(LocalDateTime.of(2005, 6, 11, 10, 0), APPEARANCE_TYPE_CODE);
        final var latestAppearance = aCourtAppearance(latestDate, APPEARANCE_TYPE_CODE);

        final var observed = CourtAppearanceBasicTransformer.latestOrSentencingCourtAppearanceOf(List.of(appearance, latestAppearance));

        shouldBeCourtAppearanceBasic(observed, latestDate, APPEARANCE_TYPE_CODE);
    }

    private CourtAppearance aCourtAppearance(LocalDateTime date, String typeCode) {
        return CourtAppearance.builder()
            .courtAppearanceId(1L)
            .appearanceDate(date)
            .court(aCourt())
            .offender(Offender.builder().crn("A123").build())
            .appearanceType(StandardReference.builder().codeDescription(APPEARANCE_TYPE_DESCRIPTION).codeValue(typeCode).build())
            .build();
    }

    private void shouldBeCourtAppearanceBasic(CourtAppearanceBasic dto, LocalDateTime date, String typeCode) {
        assertThat(dto.getCrn()).isEqualTo("A123");
        assertThat(dto.getAppearanceDate()).isEqualTo(date);
        assertThat(dto.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(dto.getCourtName()).isEqualTo(COURT_NAME);
        assertThat(dto.getAppearanceType().getCode()).isEqualTo(typeCode);
        assertThat(dto.getAppearanceType().getDescription()).isEqualTo(APPEARANCE_TYPE_DESCRIPTION);
    }

    private Court aCourt() {
        return Court.builder()
                .courtId(1L)
                .code(COURT_CODE)
                .courtName(COURT_NAME)
                .build();
    }
}
