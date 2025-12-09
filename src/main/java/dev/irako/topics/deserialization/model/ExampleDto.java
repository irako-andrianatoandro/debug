package dev.irako.topics.deserialization.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.irako.topics.deserialization.data.MultiShapeStringListDeserializer;

import java.util.List;

/**
 * Example DTO illustrating how to deserialize flexible fields into List<String> using a generic deserializer.
 */
public class ExampleDto {

    @JsonDeserialize(using = MultiShapeStringListDeserializer.class)
    private List<String> component;

    private String otherField;

    public ExampleDto() {
    }

    public List<String> getComponent() {
        return component;
    }

    public String getOtherField() {
        return otherField;
    }
}
