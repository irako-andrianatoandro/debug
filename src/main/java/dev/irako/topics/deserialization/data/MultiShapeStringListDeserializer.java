package dev.irako.topics.deserialization.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generic deserializer for List<String> that accepts multiple JSON shapes for any attribute name.
 * Supported input shapes for a property X:
 * - X: "value"
 * - X: ["v1", "v2"]
 * - X: {"X": "value"}
 * - X: [{"X": "v1"}, {"X": "v2"}]
 * - Additionally: single-field objects like {"some": "v"} will yield that single value when used inside arrays
 *   (defensive behavior to accommodate slightly varying payloads).
 */
public class MultiShapeStringListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final String currentField = p.getParsingContext() != null ? p.getParsingContext().getCurrentName() : null;
        JsonNode node = p.getCodec().readTree(p);
        // Build the result immutably (no external mutation)
        return collectValues(node, currentField);
    }

    @Override
    public List<String> getNullValue(DeserializationContext ctxt) {
        // When the JSON field is explicitly null, return an empty list rather than null
        return List.of();
    }

    private static List<String> collectValues(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return List.of();
        }

        if (node.isTextual()) {
            return List.of(node.asText());
        }

        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            // Accumulate results locally, but return an immutable copy
            List<String> acc = new ArrayList<>();
            for (JsonNode item : arrayNode) {
                acc.addAll(collectValues(item, fieldName));
            }
            return List.copyOf(acc);
        }

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;

            // Preferred: value under the same property name
            if (fieldName != null) {
                JsonNode sameName = obj.get(fieldName);
                if (sameName != null) {
                    return collectValues(sameName, fieldName);
                }
            }

            // Fallback: if it's a single-field object, use that field's textual value
            Iterator<String> names = obj.fieldNames();
            if (names.hasNext()) {
                String first = names.next();
                if (!names.hasNext()) { // only one field
                    JsonNode only = obj.get(first);
                    return collectValues(only, first);
                }
            }

            return List.of();
        }

        return List.of();
    }
}
