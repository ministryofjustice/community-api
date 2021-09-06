package uk.gov.justice.digital.delius.transformers;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import lombok.AllArgsConstructor;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static com.github.fge.jackson.jsonpointer.JsonPointer.of;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@AllArgsConstructor
public class AppointmentPatchRequestTransformer {

    private static final String NON_ATTENDANCE_VALUE = "no";
    private static final String TARGET_NOTES_FIELD_NAME = "notes";
    private static final String TARGET_OUTCOME_FIELD_NAME = "outcome";
    private static final String TARGET_ENFORCEMENT_FIELD_NAME = "enforcement";
    private static final String TARGET_OFFICE_LOCATION_FIELD_NAME = "officeLocation";

    public static JsonPatch mapAttendanceFieldsToOutcomeOf(final ContextlessAppointmentOutcomeRequest request, final IntegrationContext context) {

        final var patchOperations = new ArrayList<JsonPatchOperation>();

        patchOperations.add(new ReplaceOperation(of(TARGET_NOTES_FIELD_NAME), valueOf(request.getNotes())));

        addReplaceOperationForOutcomeIfAttended(
            context, request.getAttended(), request.getNotifyPPOfAttendanceBehaviour(), patchOperations);

        return new JsonPatch(patchOperations);
    }

    public static JsonPatch mapOfficeLocation(final String officeLocation) {

        return new JsonPatch(List.of(new ReplaceOperation(of(TARGET_OFFICE_LOCATION_FIELD_NAME), valueOf(officeLocation))));
    }

    private static void addReplaceOperationForOutcomeIfAttended(final IntegrationContext context,
                                                         final String attended,
                                                         final Boolean notifyBehaviour,
                                                         final List<JsonPatchOperation> patchOperations) {

        var mappings = context.getContactMapping()
            .getAttendanceAndBehaviourNotifiedMappingToOutcomeType();

        var outcomeType = ofNullable(mappings.get(attended.toLowerCase()))
            .map(mapping -> mapping.get(notifyBehaviour))
            .orElseThrow(() -> new IllegalStateException(
                format("Mapping does not exist for attended: %s and notify PP of behaviour: %s", attended, notifyBehaviour)));

        patchOperations.add(new ReplaceOperation(of(TARGET_OUTCOME_FIELD_NAME), valueOf(outcomeType)));

        if ( notifyBehaviour || NON_ATTENDANCE_VALUE.equalsIgnoreCase(attended)) {
            patchOperations.add(new ReplaceOperation(of(TARGET_ENFORCEMENT_FIELD_NAME),
                valueOf(context.getContactMapping().getEnforcementReferToOffenderManager())));
        }
    }
}
