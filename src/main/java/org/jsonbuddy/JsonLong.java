package org.jsonbuddy;

import java.io.PrintWriter;

public class JsonLong extends JsonSimpleValue {

    private final long value;

    public JsonLong(long value) {
        this.value = value;
    }

    @Override
    public String stringValue() {
        return Long.toString(value);
    }

    @Override
    public Object javaObjectValue() {
        if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE){
            int res = (int) value;
            return res;
        }
        return value;
    }

    @Override
    public void toJson(PrintWriter printWriter) {
        printWriter.append(stringValue());
    }

    public long longValue() {
        return value;
    }
}
