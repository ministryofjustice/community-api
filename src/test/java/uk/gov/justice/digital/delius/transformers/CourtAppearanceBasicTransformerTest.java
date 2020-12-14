package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.Court;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDateTime;

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

        CourtAppearance courtAppearance = CourtAppearance.builder()
                .courtAppearanceId(1L)
                .appearanceDate(APPEARANCE_DATE)
                .court(aCourt())
                .offender(Offender.builder().crn("A123").build())
                .appearanceType(StandardReference.builder().codeDescription(APPEARANCE_TYPE_DESCRIPTION).codeValue(APPEARANCE_TYPE_CODE).build())
                .build();

        final var dto = CourtAppearanceBasicTransformer
                .courtAppearanceOf(courtAppearance);

        assertThat(dto.getCrn()).isEqualTo("A123");
        assertThat(dto.getAppearanceDate()).isEqualTo(APPEARANCE_DATE);
        assertThat(dto.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(dto.getCourtName()).isEqualTo(COURT_NAME);
        assertThat(dto.getAppearanceType().getCode()).isEqualTo(APPEARANCE_TYPE_CODE);
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
