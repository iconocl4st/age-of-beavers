package common.state.sst.manager;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BooleanManager implements ManagerSpec<Boolean> {

    protected final HashSet<EntityId> set = new HashSet<>();

    public Set<EntityId> allEntities() {
        synchronized (set) {
            return (Set<EntityId>) set.clone();
        }
    }

    public void set(EntityId entityId, Boolean value) {
        synchronized (set) {
            if (value == null) {
                return;
            }
            if (value)
                set.add(entityId);
            else
                set.remove(entityId);
        }
    }

    public Boolean get(EntityId entityId) {
        synchronized (set) {
            return set.contains(entityId);
        }
    }

    public void remove(EntityId attacker) {
        synchronized (set) {
            set.remove(attacker);
        }
    }

    public void updateAll(ManagerSpec<Boolean> m) {
        BooleanManager manager = (BooleanManager) m;
        synchronized (set) {
            set.clear();
            set.addAll(manager.set);
        }
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        synchronized (set) {
            set.clear();
            reader.readBeginDocument();
            reader.read("set", set, EntityId.Serializer, spec);
            reader.readEndDocument();
        }
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("set", set, EntityId.Serializer, options);
        writer.writeEndDocument();
    }
}
