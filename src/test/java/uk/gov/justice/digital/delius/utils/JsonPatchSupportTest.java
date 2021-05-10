package uk.gov.justice.digital.delius.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class JsonPatchSupportTest {

    private static final List<JsonPatchOperation> REPLACE_OPERATIONS = asList(
        new ReplaceOperation(JsonPointer.of("field1"), TextNode.valueOf("value1")),
        new ReplaceOperation(JsonPointer.of("field2"), BooleanNode.valueOf(true)));

    private static final JsonPatch JSON_PATCH = new JsonPatch(REPLACE_OPERATIONS);

    private final JsonPatchSupport jsonPatchSupport = new JsonPatchSupport(new ObjectMapper());

    @Test
    public void getsStringValueUsingPath() {
        assertThat(jsonPatchSupport.getAsText("/field1", JSON_PATCH)).isEqualTo(of("value1"));
    }

    @Test
    public void getsStringValueUsingPathFromListOfNodes() {
        final var nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsText("/field1", nodes)).isEqualTo(of("value1"));
    }

    @Test
    public void getsBooleanValueUsingPathFromListOfNodes() {
        final var nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsBoolean("/field2", nodes)).isEqualTo(of(true));
    }

    @Test
    public void returnsOptionalEmptyWhenStringDoesNotExistUsingPath() {
        assertThat(jsonPatchSupport.getAsText("/field3", JSON_PATCH)).isEqualTo(empty());
    }

    @Test
    public void returnsOptionalEmptyWhenStringDoesNotExistUsingPathFromListOfNodes() {
        final var nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsText("/field3", nodes)).isEqualTo(empty());
    }

    @Test
    public void returnsOptionalEmptyWhenBooleanDoesNotExistUsingPathFromListOfNodes() {
        final var nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsBoolean("/field3", nodes)).isEqualTo(empty());
    }
}