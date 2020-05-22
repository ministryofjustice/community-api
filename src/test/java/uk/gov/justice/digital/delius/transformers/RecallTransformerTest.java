package uk.gov.justice.digital.delius.transformers;

import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.OffenderRecall;
import uk.gov.justice.digital.delius.jpa.standard.entity.Recall;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class RecallTransformerTest {

    private static final LocalDateTime SOME_DATE_TIME = LocalDateTime.now().minusDays(16);
    private static final LocalDate SOME_DATE = SOME_DATE_TIME.toLocalDate();
    private static final String SOME_REASON_CODE = "This is a reason code";
    private static final String SOME_REASON = "This is a reason";
    private static final String SOME_NOTES = "Here are some notes";

    @Test
    public void offenderRecallOf_valuesMappedCorrectly() {
        Recall recall = Recall.builder()
                .recallDate(SOME_DATE_TIME)
                .reason(StandardReference.builder().codeValue(SOME_REASON_CODE).codeDescription(SOME_REASON).build())
                .notes(SOME_NOTES)
                .build();

        OffenderRecall offenderRecall = RecallTransformer.offenderRecallOf(recall);

        assertThat(offenderRecall.getDate()).isEqualTo(SOME_DATE);
        assertThat(offenderRecall.getReason().getCode()).isEqualTo(SOME_REASON_CODE);
        assertThat(offenderRecall.getReason().getDescription()).isEqualTo(SOME_REASON);
        assertThat(offenderRecall.getNotes()).isEqualTo(SOME_NOTES);
    }
}
