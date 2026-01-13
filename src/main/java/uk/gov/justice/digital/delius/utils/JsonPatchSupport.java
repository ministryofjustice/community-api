package uk.gov.justice.digital.delius.utils;

import com.github.fge.jsonpatch.JsonPatch;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

// Jackson 3 (Spring Boot 4 default)
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Jackson 2 (used by fge json-patch)
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
@AllArgsConstructor
public class JsonPatchSupport {

    private static final String JSON_PATCH_PATH_FIELD  = "path";
    private static final String JSON_PATCH_VALUE_FIELD = "value";

    // Boot 4 injects this (Jackson 3)
    private final JsonMapper jackson3;

    // Local Jackson 2 mapper used only to deal with JsonPatch (Jackson-2–bound types)
    private static final ObjectMapper JACKSON2 = new ObjectMapper();

    public Optional<Boolean> getAsBoolean(String path, JsonNode nodes) {
        return getFieldValue(path, nodes).map(JsonNode::asBoolean);
    }

    public Optional<String> getAsText(String path, JsonNode nodes) {
        return getFieldValue(path, nodes).map(JsonNode::stringValue);
    }

    /** Reads from a JsonPatch by converting it to a Jackson-3 JsonNode array of operations. */
    public Optional<String> getAsText(String path, JsonPatch jsonPatch) {
        final JsonNode nodes = convertValue(jsonPatch, JsonNode.class);
        return getFieldValue(path, nodes).map(JsonNode::stringValue);
    }


    public <T> T convertValue(Object fromValue, Class<T> clazz) {
        if (fromValue instanceof JsonPatch && clazz == JsonNode.class) {
            return clazz.cast(convertPatchToJackson3Tree((JsonPatch) fromValue));
        }
        return jackson3.convertValue(fromValue, clazz);
    }


    private JsonNode convertPatchToJackson3Tree(JsonPatch patch) {
        try {
            // 1) Serialize the JsonPatch with Jackson 2 into the canonical array form
            final String json = JACKSON2.writeValueAsString(patch);
            // 2) Parse the JSON as a Jackson 3 tree
            return jackson3.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to convert JsonPatch to JsonNode", e);
        }
    }


    /** Jackson 3: prefer stringValue() for string comparison; add a terminal findFirst() */
    private Optional<JsonNode> getFieldValue(String path, JsonNode nodes) {
        return StreamSupport.stream(nodes.spliterator(), false)
            .filter(node -> Objects.equals(node.path(JSON_PATCH_PATH_FIELD).stringValue(), path))
            .map(node -> node.path(JSON_PATCH_VALUE_FIELD))
            .findFirst();
    }
}
