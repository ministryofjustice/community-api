package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderRecall;
import uk.gov.justice.digital.delius.jpa.standard.entity.Recall;

@Component
public class RecallTransformer {

    public OffenderRecall offenderRecallOf(Recall recall) {
        KeyValue reason = KeyValue.builder()
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
