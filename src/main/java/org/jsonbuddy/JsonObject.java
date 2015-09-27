package org.jsonbuddy;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JsonObject extends JsonNode {
    private final Map<String,JsonNode> values;


    public JsonObject() {
        this.values = new HashMap<>();
    }

    private JsonObject(Map<String,JsonNode> values) {
        this.values = values;
    }

    public Optional<String> stringValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonSimpleValue)
                .map(n -> ((JsonSimpleValue) n).stringValue());
    }

    public Optional<Long> longValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(JsonObject::isLong)
                .map(JsonObject::mapToLong);
    }

    private static boolean isLong(JsonNode jsonNode) {
        if (jsonNode instanceof JsonLong) {
            return true;
        }
        if (jsonNode instanceof JsonTextValue) {
            try {
                Long.parseLong(((JsonTextValue) jsonNode).stringValue());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private static long mapToLong(JsonNode jsonNode) {
        if (jsonNode instanceof JsonLong) {
            return ((JsonLong) jsonNode).longValue();
        }
        return Long.parseLong(jsonNode.textValue());
    }

    public Optional<Boolean> booleanValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonBooleanValue)
                .map(n -> ((JsonBooleanValue) n).boolValue());
    }

    public Optional<JsonObject> objectValue(String key) {
        return Optional.ofNullable(values.get(key))
                .filter(n -> n instanceof JsonObject)
                .map(n -> (JsonObject) n);
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
        if (value(key).isPresent() && value(key).get().equals(new JsonNullValue())) {
            return null;
        }
        return stringValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    private Supplier<JsonValueNotPresentException> throwKeyNotPresent(String key) {
        return () -> new JsonValueNotPresentException(String.format("Required key '%s' does not exsist",key));
    }


    public long requiredLong(String key) throws JsonValueNotPresentException{
        return longValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public boolean requiredBoolean(String key) throws JsonValueNotPresentException{
        return booleanValue(key).orElseThrow(throwKeyNotPresent(key));
    }

    public Instant requiredInstant(String key) {
        JsonSimpleValue val = value(key)
                .filter(no -> ((no instanceof JsonInstantValue) || (no instanceof JsonTextValue)))
                        .map(no -> (JsonSimpleValue) no)
                        .orElseThrow(throwKeyNotPresent(key));
        if (val instanceof JsonInstantValue) {
            return ((JsonInstantValue) val).instantValue();
        }
        String text = val.textValue();
        return Instant.parse(text);
    }

    public Optional<Instant> instantValue(String key) {
        Optional<JsonSimpleValue> val = value(key)
                .filter(no -> ((no instanceof JsonInstantValue) || (no instanceof JsonTextValue)))
                .map(no -> (JsonSimpleValue) no);
        if (!val.isPresent()) {
            return Optional.empty();
        }
        if (val.get() instanceof JsonInstantValue) {
            return Optional.of(((JsonInstantValue) val.get()).instantValue());
        }
        String text = val.get().textValue();
        return Optional.of(Instant.parse(text));
    }

    public JsonObject requiredObject(String key) throws JsonValueNotPresentException{
        return objectValue(key).orElseThrow(throwKeyNotPresent(key));
    }


    public JsonArray requiredArray(String key) {
        return arrayValue(key).orElseThrow(throwKeyNotPresent(key));
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
        values.put(key, jsonNode);
        return this;
    }

    public JsonObject withValue(String key,String value) {
        return withValue(key,JsonFactory.jsonText(value));
    }

    public JsonObject withValue(String key,double value) {
        return withValue(key,JsonFactory.jsonDouble(value));
    }

    public JsonObject withValue(String key,long value) {
        return withValue(key, JsonFactory.jsonLong(value));
    }

    public JsonObject withValue(String key,boolean value) {
        return withValue(key, JsonFactory.jsonBoolean(value));
    }

    public JsonObject withValue(String key,Enum<?> value) {
        return withValue(key, Optional.of(value).map(Object::toString).orElse(null));
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public JsonObject withValue(String key, List<String> values) {
        return withValue(key, JsonFactory.jsonArray().add(values));
    }

    public Optional<JsonNode> removeValue(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.remove(key));
    }

    public JsonObject withValue(String key, Instant instant) {
        return withValue(key, JsonFactory.jsonInstance(instant));
    }

    @Override
    public JsonObject deepClone() {
        Map<String, JsonNode> cloned = values.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().deepClone()));
        Map<String, JsonNode> newValues = new HashMap<>();
        newValues.putAll(cloned);
        return new JsonObject(newValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonObject)) return false;
        JsonObject that = (JsonObject) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }


}
