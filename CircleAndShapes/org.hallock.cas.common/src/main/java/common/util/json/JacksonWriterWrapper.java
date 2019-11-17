package common.util.json;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class JacksonWriterWrapper extends JsonWriterWrapperSpec {
    JsonGenerator generator;

    public JacksonWriterWrapper(JsonGenerator generator) {
        this.generator = generator;
    }


    @Override
    public void close() throws Exception {
        generator.close();
    }

    @Override
    public void writeName(String key) throws IOException {
        generator.writeFieldName(key);
    }

    @Override
    public void p_write(Boolean b) throws IOException {
        generator.writeBoolean(b);
    }

    @Override
    public void p_write(Double d) throws IOException {
        generator.writeNumber(d);
    }

    @Override
    public void p_write(Integer i) throws IOException {
        generator.writeNumber(i);
    }

    @Override
    public void p_write(String string) throws IOException {
        generator.writeString(string);
    }

    @Override
    public void writeBeginDocument() throws IOException {
        generator.writeStartObject();
    }

    @Override
    public void writeEndDocument() throws IOException {
        generator.writeEndObject();
    }

    @Override
    public void writeBeginArray() throws IOException {
        generator.writeStartArray();
    }

    @Override
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    @Override
    public void writeNull() throws IOException {
        generator.writeNull();
    }

    @Override
    public void flush() throws IOException {
        generator.flush();
    }
}
