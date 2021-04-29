package uk.gov.justice.digital.delius.transformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.utils.JsonPatchSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static com.fasterxml.jackson.databind.node.BooleanNode.valueOf;
import static com.fasterxml.jackson.databind.node.TextNode.valueOf;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class AppointmentPatchRequestTransformerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AppointmentPatchRequestTransformer appointmentPatchRequestTransformer;

    private IntegrationContext integrationContext;

    @BeforeEach
    public void before() {

        integrationContext = new IntegrationContext();
        integrationContext.getContactMapping().setAttendanceAndBehaviourNotifiedMappingToOutcomeType(
            new HashMap<>() {{
                this.put("no", new HashMap<>() {{
                    this.put(true, "AFTA");
                    this.put(false, "AFTA");
                }});
                this.put("late", new HashMap<>() {{
                    this.put(true, "AFTC");
                    this.put(false, "ATTC");
                }});
            }}
        );
        appointmentPatchRequestTransformer = new AppointmentPatchRequestTransformer(new JsonPatchSupport(objectMapper));
    }

    @Test
    public void transformsJsonPatchCollapsingAttendedAndNotifyFieldsToOutcome() throws JsonProcessingException {

        final var attendedValue = "LATE";
        final var notifyPPOfAttendanceBehaviourValue = true;
        final var jsonPatch = buildPatch(of(attendedValue), of(notifyPPOfAttendanceBehaviourValue));

        final var transformedPatch = appointmentPatchRequestTransformer.mapAttendanceFieldsToOutcomeOf(jsonPatch, integrationContext);

        assertThat(objectMapper.writeValueAsString(transformedPatch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}," +
                "{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"AFTC\"}]");
    }

    @Test
    public void doesNotTransformsJsonPatchWhenAttendedValueNotPresent() throws JsonProcessingException {

        final var notifyPPOfAttendanceBehaviourValue = true;
        final var jsonPatch = buildPatch(empty(), of(notifyPPOfAttendanceBehaviourValue));

        final var transformedPatch = appointmentPatchRequestTransformer.mapAttendanceFieldsToOutcomeOf(jsonPatch, integrationContext);

        assertThat(objectMapper.writeValueAsString(transformedPatch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}]");
    }

    @Test
    public void defaultsNotifyBehaviourToFalseWhenAttendedSetToNo() throws JsonProcessingException {

        final var attendedValue = "LATE";
        final var jsonPatch = buildPatch(of(attendedValue), empty());

        final var transformedPatch = appointmentPatchRequestTransformer.mapAttendanceFieldsToOutcomeOf(jsonPatch, integrationContext);

        assertThat(objectMapper.writeValueAsString(transformedPatch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}," +
                "{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"ATTC\"}]");
    }

    @Test
    public void throwsExceptionWhenNoMapping() {
        final var attendedValue = "UnknownValue";
        final var jsonPatch = buildPatch(of(attendedValue), empty());

        IllegalStateException illegalStateException = Assertions.assertThrows(IllegalStateException.class,
            () -> { appointmentPatchRequestTransformer.mapAttendanceFieldsToOutcomeOf(jsonPatch, integrationContext); });
        assertThat(illegalStateException.getMessage())
            .isEqualTo("Mapping does not exist for attended: UnknownValue and notify PP of behaviour: false");
    }

    private JsonPatch buildPatch(Optional<String> attendedValue, Optional<Boolean> notifyPPOfAttendanceBehaviourValue) {

        return new JsonPatch(new ArrayList<>() {{
            this.add(new ReplaceOperation(JsonPointer.of("notes"), valueOf("some notes")));
            attendedValue.ifPresent(value ->
                this.add(new ReplaceOperation(JsonPointer.of("attended"), valueOf(value))));
            notifyPPOfAttendanceBehaviourValue.ifPresent(value ->
                this.add(new ReplaceOperation(JsonPointer.of("notifyPPOfAttendanceBehaviour"), valueOf(value))));
        }});
    }
}