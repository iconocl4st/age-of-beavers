package common.state;

import common.state.sst.GameState;
import common.util.json.*;

import java.io.IOException;
import java.util.*;

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
        return ((EntityId) other).id == id;
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


    public static Set<EntityReader> getReaders(GameState state, Collection<EntityId> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) return Collections.emptySet();
        HashSet<EntityReader> ret = new HashSet<>();
        for (EntityId id : entityIds) {
            ret.add(new EntityReader(state, id));
        }
        return ret;
    }

    public static final Comparator<EntityId> COMPARATOR = Comparator.comparingInt(entityId -> entityId.id);
}
