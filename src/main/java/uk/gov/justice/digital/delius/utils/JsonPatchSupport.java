package uk.gov.justice.digital.delius.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
@AllArgsConstructor
public class JsonPatchSupport {

    private static final String JSON_PATCH_PATH_FIELD = "path";
    private static final String JSON_PATCH_VALUE_FIELD = "value";

    private final ObjectMapper objectMapper;

    public Optional<Boolean> getAsBoolean(String path, JsonNode nodes) {
        return getFieldValue(path, nodes).map(JsonNode::asBoolean);
    }

    public Optional<String> getAsText(String path, JsonNode nodes) {
        return getFieldValue(path, nodes).map(JsonNode::asText);
    }

    public Optional<String> getAsText(String path, JsonPatch jsonPatch) {
        var nodes = convertValue(jsonPatch, JsonNode.class);
        return getFieldValue(path, nodes).map(JsonNode::asText);
    }

    public <T> T convertValue(Object fromValue, Class<T> clazz) {
        return objectMapper.convertValue(fromValue, clazz);
    }

    private Optional<JsonNode> getFieldValue(String path, JsonNode nodes) {
        return StreamSupport
            .stream(nodes.spliterator(), false)
            .filter(node -> node.path(JSON_PATCH_PATH_FIELD).asText().equals(path))
            .map(node -> node.path(JSON_PATCH_VALUE_FIELD))
            .findFirst();
    }

}
