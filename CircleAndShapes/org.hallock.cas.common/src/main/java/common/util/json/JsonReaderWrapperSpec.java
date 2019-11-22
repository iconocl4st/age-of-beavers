package common.util.json;

import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public abstract class JsonReaderWrapperSpec implements AutoCloseable {
    public <T> T b(T[] t, int i) {
        if (i < 0 || i >= t.length) throw new IllegalStateException("Invalid index into enumeration: " + i);
        return t[i];
    }

    public void readBeginArray(String expectedKey) throws IOException {
        readName(expectedKey);
        readBeginArray();
    }

    public void readBeginDocument(String expectedKey) throws IOException {
        readName(expectedKey);
        readBeginDocument();
    }

    public Integer readInt32(String expectedKey) throws IOException {
        readName(expectedKey);
        return readInt32();
    }

    public Double readDouble(String expectedKey) throws IOException {
        readName(expectedKey);
        return readDouble();
    }

    public Boolean readBoolean(String expectedKey) throws IOException {
        readName(expectedKey);
        return readBoolean();
    }

    public String readString(String expectedKey) throws IOException {
        readName(expectedKey);
        return readString();
    }

    public Long readLong(String flags) throws IOException {
        readName(flags);
        return readLong();
    }

    public boolean hasMoreInArray() {
        return !getCurrentJacksonType().equals(JsonToken.END_ARRAY);
    }

    public boolean isBeginDocument() {
        return getCurrentJacksonType().equals(JsonToken.START_OBJECT);
    }

    public <T> T read(String expectedKey, DataSerializer<T> serializer, ReadOptions spec) throws IOException {
        readName(expectedKey);
        return read(serializer, spec);
    }

    // TODO: move the spec to the last argument...
    public <T> T[] read(String expectedKey, T[] map, DataSerializer<T> serializer, ReadOptions spec) throws IOException {
        readName(expectedKey);
        if (getCurrentJacksonType().equals(JsonToken.VALUE_NULL)) {
            readNull();
            return null;
        }
        readBeginArray();
        LinkedList<T> list = new LinkedList<>();
        while (hasMoreInArray()) {
            list.add(read(serializer, spec));
        }
        readEndArray();
        return list.toArray(map);
    }

    public <T> Collection<T> read(String expectedKey, Collection<T> map, DataSerializer<T> serializer, ReadOptions spec) throws IOException {
        readName(expectedKey);
        if (getCurrentJacksonType().equals(JsonToken.VALUE_NULL)) {
            readNull();
            return null;
        }
        readBeginArray();
        while (hasMoreInArray()) {
            map.add(read(serializer, spec));
        }
        readEndArray();
        return map;
    }

    public <T, G> Map<T, G>read(String expectedKey, Map<T, G> map, DataSerializer<T> keySerializer, DataSerializer<G> valueSerializer, ReadOptions spec) throws IOException {
        readName(expectedKey);
        readBeginArray();
        while (hasMoreInArray()) {
            readBeginDocument();
            T key = read("key", keySerializer, spec);
            G value = read("value", valueSerializer, spec);
            map.put(key, value);
            readEndDocument();
        }
        readEndArray();
        return map;
    }

    public <T> T read(DataSerializer<T> serializer, ReadOptions spec) throws IOException {
        if (getCurrentJacksonType().equals(JsonToken.VALUE_NULL)) {
            readNull();
            return null;
        }
        return serializer.parse(this, spec);
    }

    public abstract JsonToken getCurrentJacksonType();

    public abstract void readName(String expectedName) throws IOException;

    public abstract Boolean readBoolean() throws IOException;
    public abstract Double readDouble() throws IOException;
    public abstract Integer readInt32() throws IOException;
    public abstract String readString() throws IOException;
    public abstract void readNull() throws IOException;
    public abstract Long readLong() throws IOException;

    public abstract void readBeginDocument() throws IOException;
    public abstract void readEndDocument() throws IOException;

    public abstract void readBeginArray() throws IOException;
    public abstract void readEndArray() throws IOException;

    public abstract void finishCurrentObject() throws IOException;
    // Try not to use this...
    public abstract String awkwardlyReadName() throws IOException;
}
