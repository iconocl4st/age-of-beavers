package common.state.sst.manager;

import common.state.EntityId;
import common.util.json.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ManagerImpl<T> implements ManagerSpec<T> {

    protected final HashMap<EntityId, T> map = new HashMap<>();
    protected final DataSerializer<T> serializer;

    public ManagerImpl(DataSerializer<T> serializer) {
        this.serializer = serializer;
    }

    // to do rename, all entities
    public Set<EntityId> allKeys() {
        synchronized (map) {
            return new HashSet<>(map.keySet());
        }
    }

    public void set(EntityId entityId, T value) {
        synchronized (map) {
            map.put(entityId, value);
        }
    }

    public T get(EntityId entityId) {
        synchronized (map) {
            return map.get(entityId);
        }
    }

    public void remove(EntityId attacker) {
        synchronized (map) {
            map.remove(attacker);
        }
    }

    public void updateAll(ManagerSpec<T> m) {
        ManagerImpl<T> manager = (ManagerImpl<T>) m;
        synchronized (map) {
            removeAll();
            for (Map.Entry<EntityId, T> entry :  ((ManagerImpl<T>) m).map.entrySet()) {
                set(entry.getKey(), entry.getValue());
            }
        }
    }

    protected void removeAll() {
        map.clear();
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        synchronized (map) {
            removeAll();
            reader.readBeginDocument();
            reader.readName("map");
            reader.readBeginArray();
            while (reader.hasMoreInArray()) {
                reader.readBeginDocument();
                EntityId key = reader.read("key", EntityId.Serializer, spec);
                T value = reader.read("value", serializer, spec);
                reader.readEndDocument();
                set(key, value);
            }
            reader.readEndArray();
            reader.readEndDocument();
        }
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        synchronized (map) {
            writer.writeBeginDocument();
            writer.write("map", map, EntityId.Serializer, serializer, options);
            writer.writeEndDocument();
        }
    }
}
