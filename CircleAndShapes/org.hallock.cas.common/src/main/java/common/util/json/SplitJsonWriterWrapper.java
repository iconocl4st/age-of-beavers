package common.util.json;

import java.io.IOException;

public class SplitJsonWriterWrapper extends  JsonWriterWrapperSpec {

    private final JsonWriterWrapperSpec r1;
    private final JsonWriterWrapperSpec r2;

    public SplitJsonWriterWrapper(JsonWriterWrapperSpec r1, JsonWriterWrapperSpec r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    @Override
    public void writeName(String value) throws IOException {
        r1.writeName(value); r2.writeName(value);
    }

    @Override
    protected void p_write(Boolean value) throws IOException {
        r1.p_write(value); r2.p_write(value);
    }

    @Override
    protected void p_write(Double value) throws IOException {
        r1.p_write(value); r2.p_write(value);
    }

    @Override
    protected void p_write(Integer value) throws IOException {
        r1.p_write(value); r2.p_write(value);
    }

    @Override
    protected void p_write(String value) throws IOException {
        r1.p_write(value); r2.p_write(value);
    }

    @Override
    public void writeBeginDocument() throws IOException {
        r1.writeBeginDocument(); r2.writeBeginDocument();
    }

    @Override
    public void writeEndDocument() throws IOException {
        r1.writeEndDocument(); r2.writeEndDocument();
    }

    @Override
    public void writeBeginArray() throws IOException {
        r1.writeBeginArray(); r2.writeBeginArray();
    }

    @Override
    public void writeEndArray() throws IOException {
        r1.writeEndArray(); r2.writeEndArray();
    }

    @Override
    public void writeNull() throws IOException {
        r1.writeNull(); r2.writeNull();
    }

    @Override
    public void flush() throws IOException {
        r1.flush(); r2.flush();
    }

    @Override
    public void close() throws Exception {
        r1.close(); r2.close();
    }
}
