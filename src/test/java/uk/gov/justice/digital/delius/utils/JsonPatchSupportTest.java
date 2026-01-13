
package uk.gov.justice.digital.delius.utils;

import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class JsonPatchSupportTest {

    private static final List<JsonPatchOperation> REPLACE_OPERATIONS = asList(
        new ReplaceOperation(JsonPointer.of("field1"), TextNode.valueOf("value1")),
        new ReplaceOperation(JsonPointer.of("field2"), BooleanNode.valueOf(true))
    );

    private static final JsonPatch JSON_PATCH = new JsonPatch(REPLACE_OPERATIONS);

    private final JsonPatchSupport jsonPatchSupport =
        new JsonPatchSupport(JsonMapper.builder().build());

    @Test
    void getsStringValueUsingPath() {
        assertThat(jsonPatchSupport.getAsText("/field1", JSON_PATCH))
            .isEqualTo(of("value1"));
    }

    @Test
    void getsStringValueUsingPathFromListOfNodes() {
        final JsonNode nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsText("/field1", nodes))
            .isEqualTo(of("value1"));
    }

    @Test
    void getsBooleanValueUsingPathFromListOfNodes() {
        final JsonNode nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsBoolean("/field2", nodes))
            .isEqualTo(of(true));
    }

    @Test
    void returnsOptionalEmptyWhenStringDoesNotExistUsingPath() {
        assertThat(jsonPatchSupport.getAsText("/field3", JSON_PATCH))
            .isEqualTo(empty());
    }

    @Test
    void returnsOptionalEmptyWhenStringDoesNotExistUsingPathFromListOfNodes() {
        final JsonNode nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsText("/field3", nodes))
            .isEqualTo(empty());
    }

    @Test
    void returnsOptionalEmptyWhenBooleanDoesNotExistUsingPathFromListOfNodes() {
        final JsonNode nodes = jsonPatchSupport.convertValue(JSON_PATCH, JsonNode.class);
        assertThat(jsonPatchSupport.getAsBoolean("/field3", nodes))
            .isEqualTo(empty());
    }
}
