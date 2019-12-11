package common.util.json;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public abstract class JsonWriterWrapperSpec implements AutoCloseable {

    public void writeBeginArray(String keyName) throws IOException {
        writeName(keyName);
        writeBeginArray();
    }

    public void writeBeginDocument(String keyName) throws IOException {
        writeName(keyName);
        writeBeginDocument();
    }

    public void write(String name, long value) throws IOException {
        writeName(name);
        write(value);
    }

    public void write(String keyName, Integer value) throws IOException {
        writeName(keyName);
        write(value);
    }

    public void write(String keyName, Double value) throws IOException {
        writeName(keyName);
        write(value);
    }

    public void write(String keyName, Boolean value) throws IOException {
        writeName(keyName);
        write(value);
    }

    public void write(String keyName, String value) throws IOException {
        writeName(keyName);
        write(value);
    }

    public <T> void write(String keyName, T[] map, DataSerializer<T> serializer, WriteOptions options) throws IOException {
        writeName(keyName);
        if (map == null) {
            writeNull();
            return;
        }
        writeBeginArray();
        for (T t : map) {
            serializer.write(t, this, options);
        }
        writeEndArray();
    }

    public <T> void write(String keyName, Collection<T> map, DataSerializer<T> serializer, WriteOptions options) throws IOException {
        writeName(keyName);
        if (map == null) {
            writeNull();
            return;
        }
        writeBeginArray();
        for (T t : map) {
            serializer.write(t, this, options);
        }
        writeEndArray();
    }

    public <T> void write(String keyName, T t, DataSerializer<T> serializer, WriteOptions options) throws IOException {
        writeName(keyName);
        write(t, serializer, options);
    }

    public <T, G> void write(String keyName, Map<T, G> map, DataSerializer<T> keySerializer, DataSerializer<G> valueSerializer, WriteOptions options) throws IOException {
        writeName(keyName);
        if (map == null) {
            writeNull();
            return;
        }
        writeBeginArray();
        for (Map.Entry<T, G> entry : map.entrySet()) {
            writeBeginDocument();
            write("key", entry.getKey(), keySerializer, options);
            write("value", entry.getValue(), valueSerializer, options);
            writeEndDocument();
        }
        writeEndArray();
    }

    public <T> void write(T t, DataSerializer<T> serializer, WriteOptions options) throws IOException {
        if (t == null) { writeNull(); return; }
        serializer.write(t, this, options);
    }


    public void write(Long l) throws IOException {
        if (l == null) { writeNull(); return; }
        p_write(l);
    }


    public void write(Boolean v) throws IOException {
        if (v == null) { writeNull(); return; }
        p_write(v);
    }
    public void write(Double v) throws IOException {
        if (v == null) { writeNull(); return; }
        p_write(v);
    }
    public void write(Integer v) throws IOException {
        if (v == null) { writeNull(); return; }
        p_write(v);
    }
    public void write(String v) throws IOException {
        if (v == null) { writeNull(); return; }
        p_write(v);
    }

    public abstract void writeName(String key) throws IOException;

    protected abstract void p_write(Boolean b) throws IOException;
    protected abstract void p_write(Double d) throws IOException;
    protected abstract void p_write(Integer i) throws IOException;
    protected abstract void p_write(String string) throws IOException;
    protected abstract void p_write(Long l) throws IOException;

    public abstract void writeBeginDocument() throws IOException;
    public abstract void writeEndDocument() throws IOException;

    public abstract void writeBeginArray() throws IOException;
    public abstract void writeEndArray() throws IOException;

    public abstract void writeNull() throws IOException;

    public abstract void flush() throws IOException;
}
