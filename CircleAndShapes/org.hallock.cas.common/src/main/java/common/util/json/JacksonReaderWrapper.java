package common.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class JacksonReaderWrapper extends JsonReaderWrapperSpec {

    private final JsonParser parser;
    private boolean initialized;

    public JacksonReaderWrapper(JsonParser parser) {
        this.parser = parser;
    }

    public void finishCurrentObject() throws IOException {
        int depth = 1;
        JsonToken currentToken = getCurrentJacksonType();
        while (depth > 1 || !currentToken.equals(JsonToken.END_OBJECT)) {
            switch (currentToken = parser.nextToken()) {
                case START_OBJECT: ++depth; break;
                case END_OBJECT: --depth; break;
            }
        }
    }

    @Override
    public JsonToken getCurrentJacksonType() {
        return parser.getCurrentToken();
    }

    @Override
    public void readName(String expectedName) throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        if (!jsonToken.equals(JsonToken.FIELD_NAME))
            throw new IllegalStateException("Expected name: " + expectedName + ", but found a token of type " + jsonToken.name());
        if (!parser.getCurrentName().equals(expectedName)) {
            throw new IllegalStateException("Expected name: " + expectedName + ", Found name: " + parser.getCurrentName());
        }
        parser.nextToken();
    }

//    private String printAcceptable(JsonToken[] tokens) {
//        StringBuilder builder = new StringBuilder();
//        for (JsonToken token : tokens) {
//            builder.append(token.name()).append(", ");
//        }
//        return builder.toString();
//    }
//
//    private void assertTokenIsOneOf(JsonToken token, JsonToken... acceptableTokens) {
//        for (JsonToken acceptable : acceptableTokens) {
//            if (token.equals(acceptable))
//                return;
//        }
//        throw new IllegalStateException("Expected token to be one of: [" + printAcceptable(acceptableTokens) + "], but found: " + token);
//    }

    //        assertTokenIsOneOf(jsonToken)

    @Override
    public Boolean readBoolean() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        switch (jsonToken) {
            case VALUE_FALSE:
                parser.nextToken();
                return Boolean.FALSE;
            case VALUE_TRUE:
                parser.nextToken();
                return Boolean.TRUE;
            case VALUE_NULL:
                parser.nextToken();
                return null;
            default:
                throw new IllegalStateException("Expected token to be boolean, but found: " + jsonToken);
        }
    }

    @Override
    public Double readDouble() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        switch (jsonToken) {
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
                Double value = parser.getDoubleValue();
                parser.nextToken();
                return value;
            case VALUE_NULL:
                parser.nextToken();
                return null;
            default:
                throw new IllegalStateException("Expected token to be double, but found: " + jsonToken);
        }
    }

    @Override
    public Integer readInt32() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        switch (jsonToken) {
            case VALUE_NUMBER_INT:
                Integer intValue = parser.getIntValue();
                parser.nextToken();
                return intValue;
            case VALUE_NULL:
                parser.nextToken();
                return null;
            default:
                throw new IllegalStateException("Expected token to be int, but found: " + jsonToken);
        }
    }

    @Override
    public String readString() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        switch (jsonToken) {
            case VALUE_STRING:
                String value = parser.getValueAsString();
                parser.nextToken();
                return value;
            case VALUE_NULL:
                parser.nextToken();
                return null;
            default:
                throw new IllegalStateException("Expected token to be String, but found: " + jsonToken);
        }
    }

    @Override
    public void readNull() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        if (!jsonToken.equals(JsonToken.VALUE_NULL))
            throw new IllegalStateException("Expected token to be null, but found: " + jsonToken);
        parser.nextToken();
    }

    @Override
    public void readBeginDocument() throws IOException {
        if (!initialized) { parser.nextToken(); initialized = true; }
        JsonToken jsonToken = parser.getCurrentToken();
        if (!jsonToken.equals(JsonToken.START_OBJECT))
            throw new IllegalStateException("Expected token to be begin document, but found: " + jsonToken);
        parser.nextToken();
    }

    @Override
    public void readEndDocument() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        if (!jsonToken.equals(JsonToken.END_OBJECT))
            throw new IllegalStateException("Expected token to be end document, but found: " + jsonToken);
        parser.nextToken();
    }

    @Override
    public void readBeginArray() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        if (!jsonToken.equals(JsonToken.START_ARRAY))
            throw new IllegalStateException("Expected token to be begin array, but found: " + jsonToken);
        parser.nextToken();
    }

    @Override
    public void readEndArray() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        if (!jsonToken.equals(JsonToken.END_ARRAY))
            throw new IllegalStateException("Expected token to be end array, but found: " + jsonToken);
        parser.nextToken();
    }

    @Override
    public String awkwardlyReadName() throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        if (!jsonToken.equals(JsonToken.FIELD_NAME))
            throw new IllegalStateException("Expected token to be a name, but found: " + jsonToken);
        String name = parser.getCurrentName();
        parser.nextToken();
        return name;
    }

    @Override
    public void close() throws Exception {
        parser.close();
    }
}
