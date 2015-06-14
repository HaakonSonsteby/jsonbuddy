package org.jsonbuddy;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class JsonObject extends JsonNode {
    private final Map<String,JsonNode> values = new HashMap<>();


    public JsonObject() {

    }

    public Optional<String> stringValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonSimpleValue)
                .map(n -> ((JsonSimpleValue) n).stringValue());
    }

    public Optional<JsonArray> arrayValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonArray)
                .map(n -> (JsonArray) n);

    }

    public Optional<JsonNode> value(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public String requiredString(String key) throws JsonValueNotPresentException {
        Optional<String> val = stringValue(key);
        if (!val.isPresent()) {
            throw new JsonValueNotPresentException(String.format("Required key '%s' does not exsist",key));
        }
        return val.get();
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append("{");
        boolean notFirst = false;
        for (Map.Entry<String,JsonNode> entry : values.entrySet()) {
            if (notFirst) {
                printWriter.append(",");
            }
            notFirst = true;
            printWriter.append('"');
            printWriter.append(entry.getKey());
            printWriter.append("\":");
            entry.getValue().toJson(printWriter);
        }

        printWriter.append("}");
    }

    public JsonObject withValue(String key, JsonNode jsonNode) {
        values.put(key,jsonNode);
        return this;
    }

    public JsonObject withValue(String key,String value) {
        return withValue(key,JsonFactory.jsonText(value));
    }

    public Set<String> keys() {
        return values.keySet();
    }
}
