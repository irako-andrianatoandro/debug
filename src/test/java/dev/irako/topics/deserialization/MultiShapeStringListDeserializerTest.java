package dev.irako.topics.deserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.irako.topics.deserialization.model.ExampleDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MultiShapeStringListDeserializer – supported JSON shapes")
public class MultiShapeStringListDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("component field")
    class ComponentCases {
        @Test
        @DisplayName("string value -> [value]")
        void deserializesStringForm() throws Exception {
            String json = "{\"component\":\"value\"}";
            ExampleDto dto = mapper.readValue(json, ExampleDto.class);
            assertEquals(List.of("value"), dto.getComponent());
        }

        @Test
        @DisplayName("string value -> [value], other fields")
        void deserializesStringWithAttributesForm() throws Exception {
            String json = "{\"component\":\"value\", \"otherField\":\"otherValue\"}";
            ExampleDto dto = mapper.readValue(json, ExampleDto.class);
            assertEquals(List.of("value"), dto.getComponent());
            assertEquals("otherValue", dto.getOtherField());
        }

        @Test
        @DisplayName("array of objects -> [value1, value2]")
        void deserializesArrayOfObjectsForm() throws Exception {
            String json = "{\"component\":[{\"component\":\"value1\"},{\"component\":\"value2\"}]}";
            ExampleDto dto = mapper.readValue(json, ExampleDto.class);
            assertEquals(List.of("value1", "value2"), dto.getComponent());
        }

        @Test
        @DisplayName("array of strings -> [a, b]")
        void alsoSupportsArrayOfStrings() throws Exception {
            String json = "{\"component\":[\"a\",\"b\"]}";
            ExampleDto dto = mapper.readValue(json, ExampleDto.class);
            assertEquals(List.of("a", "b"), dto.getComponent());
        }

        @Test
        @DisplayName("single object {component: 'single'} -> ['single']")
        void genericAlsoSupportsSingleObjectForm() throws Exception {
            String json = "{\"component\":{\"component\":\"single\"}}";
            ExampleDto dto = mapper.readValue(json, ExampleDto.class);
            assertEquals(List.of("single"), dto.getComponent());
        }
    }


    @Nested
    @DisplayName("null and edge cases")
    class NullAndEdgeCases {
        @Test
        @DisplayName("{ component: null } -> [] (empty list)")
        void nullValueYieldsEmptyList() throws Exception {
            String json = "{\"component\":null}";
            ExampleDto dto = mapper.readValue(json, ExampleDto.class);
            assertEquals(List.of(), dto.getComponent());
        }
    }
}
