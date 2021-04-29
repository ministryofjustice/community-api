package uk.gov.justice.digital.delius.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.utils.JsonPatchSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static com.github.fge.jackson.jsonpointer.JsonPointer.of;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Component
@AllArgsConstructor
public class AppointmentPatchRequestTransformer {

    private static final String JSON_PATCH_PATH_FIELD = "path";

    private static final String SOURCE_ATTENDED_FIELD_PATH = "/attended";
    private static final String SOURCE_NOTIFY_PP_FIELD_PATH = "/notifyPPOfAttendanceBehaviour";
    private static final String TARGET_OUTCOME_FIELD_NAME = "outcome";

    private final JsonPatchSupport jsonPatchSupport;

    public JsonPatch mapAttendanceFieldsToOutcomeOf(final JsonPatch jsonPatch, final IntegrationContext context) {

        var nodes = jsonPatchSupport.convertValue(jsonPatch, JsonNode.class);

        var attendedNode = jsonPatchSupport.getAsText(SOURCE_ATTENDED_FIELD_PATH, nodes);
        var notifyBehaviourNode = jsonPatchSupport.getAsBoolean(SOURCE_NOTIFY_PP_FIELD_PATH, nodes);

        var otherReplaceOperations = StreamSupport
            .stream(nodes.spliterator(), false)
            .filter(node -> {
                var fieldPath = node.path(JSON_PATCH_PATH_FIELD).asText();
                return !SOURCE_ATTENDED_FIELD_PATH.equals(fieldPath) &&
                    !SOURCE_NOTIFY_PP_FIELD_PATH.equals(fieldPath);
            })
            .map(node -> jsonPatchSupport.convertValue(node, JsonPatchOperation.class))
            .collect(toList());

        var replaceOperationsWithOutcome = addReplaceOperationForOutcomeIfAttended(
            context, attendedNode, notifyBehaviourNode, otherReplaceOperations);

        return new JsonPatch(replaceOperationsWithOutcome);
    }

    private List<JsonPatchOperation> addReplaceOperationForOutcomeIfAttended(final IntegrationContext context,
                                                                             final Optional<String> attendedNode,
                                                                             final Optional<Boolean> notifyBehaviourNode,
                                                                             final List<JsonPatchOperation> originalReplaceOperations) {

        var replaceOperations = new ArrayList<>(originalReplaceOperations);

        if ( attendedNode.isPresent() ) {
            var mappings = context.getContactMapping()
                .getAttendanceAndBehaviourNotifiedMappingToOutcomeType();

            var attendedType = attendedNode.orElseThrow(
                () -> new IllegalStateException("Attended does not exist in json patch"));
            var notifyBehaviour = notifyBehaviourNode.orElse(false);

            var outcomeType = ofNullable(mappings.get(attendedType.toLowerCase()))
                .map(mapping -> mapping.get(notifyBehaviour))
                .orElseThrow(() -> new IllegalStateException(
                    format("Mapping does not exist for attended: %s and notify PP of behaviour: %s", attendedType, notifyBehaviour)));

            replaceOperations.add(new ReplaceOperation(of(TARGET_OUTCOME_FIELD_NAME), valueOf(outcomeType)));
        }

        return replaceOperations;
    }

}
