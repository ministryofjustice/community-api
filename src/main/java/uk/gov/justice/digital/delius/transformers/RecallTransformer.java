package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderRecall;
import uk.gov.justice.digital.delius.jpa.standard.entity.Recall;

public class RecallTransformer {

    public static OffenderRecall offenderRecallOf(Recall recall) {
        final var reason = KeyValue.builder()
                .code(recall.getReason().getCodeValue())
                .description(recall.getReason().getCodeDescription())
                .build();
        return OffenderRecall.builder()
                .date(recall.getRecallDate().toLocalDate())
                .reason(reason)
                .notes(recall.getNotes())
                .build();
    }
}
