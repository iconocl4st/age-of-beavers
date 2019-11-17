package common.state;

import common.util.json.*;

import java.io.IOException;

public class EntityId implements Jsonable {
    public final int id;
    // TODO: Could have an index, and then all of the managers just have an array...

    public static final EntityId NONE = new EntityId(0);

    public EntityId(int id) {
        this.id = id;
    }

    public String toString() {
        return String.valueOf(id);
    }

    public int hashCode() {
        return Integer.hashCode(id);
    }

    public boolean equals(Object  other) {
        if (!(other instanceof EntityId)) {
            return false;
        }
        if (((EntityId) other).id != id) {
            return false;
        }
        return true;
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.write(id);
    }

    public static final DataSerializer<EntityId> Serializer = new DataSerializer.JsonableSerializer<EntityId>() {
        @Override
        public EntityId parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            return new EntityId(reader.readInt32());
        }
    };
}
