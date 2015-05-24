package org.jsonbuddy;

public class JsonSimpleValueFactory extends JsonFactory {
    private String value;

    public static JsonSimpleValueFactory text(String value) {
        JsonSimpleValueFactory jsonSimpleValueFactory = new JsonSimpleValueFactory();
        jsonSimpleValueFactory.value = value;
        return jsonSimpleValueFactory;
    }

    public JsonSimpleValue create() {
        return new JsonSimpleValue(this);
    }

    public String getValue() {
        return value;
    }
}