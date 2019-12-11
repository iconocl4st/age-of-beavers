package common.state.sst.manager;

import com.sun.org.apache.xerces.internal.dom.EntityImpl;
import common.state.EntityId;
import common.util.json.*;

import java.io.IOException;
import java.util.Map;

public class EntityManager extends ManagerImpl<Object> {
    public EntityManager() {
        super(null);
    }

    @Override
    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        synchronized (map) {
            removeAll();
            reader.readBeginDocument();
            reader.readName("map");
            reader.readBeginArray();
            while (reader.hasMoreInArray())
                set(reader.read(EntityId.Serializer, spec), new Object());
            reader.readEndArray();
            reader.readEndDocument();
        }
    }

    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        synchronized (map) {
            writer.writeBeginDocument();
            writer.writeBeginArray("map");
            for (Map.Entry<EntityId, Object> e : map.entrySet())
                writer.write(e.getKey(), EntityId.Serializer, options);
            writer.writeEndArray();
            writer.writeEndDocument();
        }
    }
}
