package uk.gov.justice.digital.delius.transformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentOutcomeRequest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.transformers.AppointmentPatchRequestTransformer.mapAttendanceFieldsToOutcomeOf;
import static uk.gov.justice.digital.delius.transformers.AppointmentPatchRequestTransformer.mapOfficeLocation;

class AppointmentPatchRequestTransformerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private IntegrationContext integrationContext;

    @BeforeEach
    public void before() {

        integrationContext = new IntegrationContext();
        integrationContext.getContactMapping().setEnforcementReferToOffenderManager("ROM");
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
    }

    @Test
    public void transformsJsonPatchCollapsingAttendedAndNotifyFieldsToOutcome() throws JsonProcessingException {

        final var attendedValue = "LATE";
        final var notifyPPOfAttendanceBehaviourValue = false;
        final var request = buildRequest(attendedValue, notifyPPOfAttendanceBehaviourValue);

        final var patch = mapAttendanceFieldsToOutcomeOf(request, integrationContext);

        assertThat(objectMapper.writeValueAsString(patch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}," +
                "{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"ATTC\"}]");
    }

    @Test
    public void transformsJsonPatchCollapsingAttendedAndNotifyFieldsToOutcomeWithEnforcement() throws JsonProcessingException {

        final var attendedValue = "LATE";
        final var notifyPPOfAttendanceBehaviourValue = true;
        final var request = buildRequest(attendedValue, notifyPPOfAttendanceBehaviourValue);

        final var patch = mapAttendanceFieldsToOutcomeOf(request, integrationContext);

        assertThat(objectMapper.writeValueAsString(patch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}," +
                "{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"AFTC\"}," +
                "{\"op\":\"replace\",\"path\":\"/enforcement\",\"value\":\"ROM\"}]");
    }

    @Test
    public void transformsJsonPatchCollapsingNonAttendedToOutcomeWithEnforcement() throws JsonProcessingException {

        final var attendedValue = "No";
        final var notifyPPOfAttendanceBehaviourValue = false;
        final var request = buildRequest(attendedValue, notifyPPOfAttendanceBehaviourValue);

        final var patch = mapAttendanceFieldsToOutcomeOf(request, integrationContext);

        assertThat(objectMapper.writeValueAsString(patch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}," +
                "{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"AFTA\"}," +
                "{\"op\":\"replace\",\"path\":\"/enforcement\",\"value\":\"ROM\"}]");
    }

    @Test
    public void transformsJsonPatchCollapsingAttendedToOutcomeWithoutEnforcement() throws JsonProcessingException {

        final var attendedValue = "lAtE";
        final var notifyPPOfAttendanceBehaviourValue = false;
        final var request = buildRequest(attendedValue, notifyPPOfAttendanceBehaviourValue);

        final var patch = mapAttendanceFieldsToOutcomeOf(request, integrationContext);

        assertThat(objectMapper.writeValueAsString(patch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}," +
                "{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"ATTC\"}]");
    }

    @Test
    public void transformsJsonPatchUsingOfficeLocation() throws JsonProcessingException {

        final var patch = mapOfficeLocation("CRSLOND");

        assertThat(objectMapper.writeValueAsString(patch))
            .isEqualTo("[{\"op\":\"replace\",\"path\":\"/officeLocation\",\"value\":\"CRSLOND\"}]");
    }

    @Test
    public void throwsExceptionWhenNoMapping() {
        final var attendedValue = "UnknownValue";
        final var request = buildRequest(attendedValue, true);

        IllegalStateException illegalStateException = Assertions.assertThrows(IllegalStateException.class,
            () -> { mapAttendanceFieldsToOutcomeOf(request, integrationContext); });
        assertThat(illegalStateException.getMessage())
            .isEqualTo("Mapping does not exist for attended: UnknownValue and notify PP of behaviour: true");
    }

    private ContextlessAppointmentOutcomeRequest buildRequest(String attended, Boolean notifyPPOfAttendanceBehaviour) {
        return ContextlessAppointmentOutcomeRequest.builder()
            .notes("some notes")
            .attended(attended)
            .notifyPPOfAttendanceBehaviour(notifyPPOfAttendanceBehaviour)
            .build();
    }
}