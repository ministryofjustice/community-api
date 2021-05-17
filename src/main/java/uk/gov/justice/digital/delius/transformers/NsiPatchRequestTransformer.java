package uk.gov.justice.digital.delius.transformers;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralEndRequest;

import java.util.ArrayList;

import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static com.github.fge.jackson.jsonpointer.JsonPointer.of;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.justice.digital.delius.utils.DateConverter.toLondonLocalDateTime;

@Component
@AllArgsConstructor
public class NsiPatchRequestTransformer {

    private static final String TARGET_NSI_OUTCOME_FIELD_NAME = "outcome";
    private static final String TARGET_END_DATE_TIME_FIELD_NAME = "endDate";
    private static final String TARGET_NOTES_FIELD_NAME = "notes";

    public JsonPatch mapEndTypeToOutcomeOf(final ContextlessReferralEndRequest request, final IntegrationContext context) {

        final var endTypeToOutcomeTypeMapping = context.getContactMapping().getEndTypeToOutcomeType();
        final var outcomeType = ofNullable(endTypeToOutcomeTypeMapping.get(request.getEndType()))
            .orElseThrow(() -> new IllegalStateException(format("Mapping does not exist for referral end type: %s", request.getEndType())));

        final var patchOperations = new ArrayList<JsonPatchOperation>();
        patchOperations.add(new ReplaceOperation(of(TARGET_NSI_OUTCOME_FIELD_NAME), valueOf(outcomeType)));
        patchOperations.add(new ReplaceOperation(of(TARGET_END_DATE_TIME_FIELD_NAME), valueOf(toLondonLocalDateTime(request.getEndedAt()).toString()))); // ISO-8601 format
        patchOperations.add(new ReplaceOperation(of(TARGET_NOTES_FIELD_NAME), valueOf(request.getNotes())));

        return new JsonPatch(patchOperations);
    }
}
