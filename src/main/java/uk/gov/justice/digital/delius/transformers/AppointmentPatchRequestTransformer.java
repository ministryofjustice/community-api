package uk.gov.justice.digital.delius.transformers;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import lombok.AllArgsConstructor;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
public class AppointmentPatchRequestTransformer {

    private static final String TARGET_NOTES_FIELD_NAME = "notes";
    private static final String TARGET_OUTCOME_FIELD_NAME = "outcome";
    private static final String TARGET_ENFORCEMENT_FIELD_NAME = "enforcement";
    private static final String TARGET_OFFICE_LOCATION_FIELD_NAME = "officeLocation";

    public static JsonPatch mapAttendanceFieldsToOutcomeOf(final ContextlessAppointmentOutcomeRequest request, final IntegrationContext context) {

        final var patchOperations = new ArrayList<JsonPatchOperation>();

        patchOperations.add(new ReplaceOperation(JsonPointer.of(TARGET_NOTES_FIELD_NAME), valueOf(request.getNotes())));

        addReplaceOperationForOutcomeIfAttended(
            context, request.getAttended(), request.getNotifyPPOfAttendanceBehaviour(), patchOperations);

        return new JsonPatch(patchOperations);
    }

    public static JsonPatch mapOfficeLocation(final String officeLocation) {

        return new JsonPatch(List.of(new ReplaceOperation(JsonPointer.of(TARGET_OFFICE_LOCATION_FIELD_NAME), valueOf(officeLocation))));
    }

    private static void addReplaceOperationForOutcomeIfAttended(final IntegrationContext context,
                                                         final String attended,
                                                         final Boolean notifyBehaviour,
                                                         final List<JsonPatchOperation> patchOperations) {

        getOutcomeType(context, attended, notifyBehaviour).ifPresent(
            outcomeType -> patchOperations.add(new ReplaceOperation(JsonPointer.of(TARGET_OUTCOME_FIELD_NAME), valueOf(outcomeType)))
        );

        getEnforcementReferToOffenderManager(context, notifyBehaviour).ifPresent(
            enforcement -> patchOperations.add(new ReplaceOperation(JsonPointer.of(TARGET_ENFORCEMENT_FIELD_NAME), valueOf(enforcement)))
        );
    }

    public static Optional<String> getOutcomeType(IntegrationContext context, String attended, Boolean notifyBehaviour) {
        if ( attended == null ) return empty();
        var mappings= context.getContactMapping()
            .getAttendanceAndBehaviourNotifiedMappingToOutcomeType();

        return ofNullable(mappings.get(attended.toLowerCase()))
            .map(mapping -> ofNullable(mapping.get(notifyBehaviour)))
            .orElseThrow(() -> new IllegalStateException(
                format("Mapping does not exist for attended: %s and notify PP of behaviour: %s", attended, notifyBehaviour)));
    }

    public static Optional<String> getEnforcementReferToOffenderManager(IntegrationContext context, Boolean notifyBehaviour) {
        if ( notifyBehaviour == null ) return empty();
        return notifyBehaviour ? of(context.getContactMapping().getEnforcementReferToOffenderManager()) : empty();
    }
}
