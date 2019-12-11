package common.state.sst.manager;

import common.state.EntityId;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.Jsonable;
import common.util.json.ReadOptions;

import java.io.IOException;

public interface ManagerSpec<T> extends Jsonable {
    void set(EntityId entityId, T value);
    T get(EntityId entityId);
    void remove(EntityId attacker);
    void updateAll(ManagerSpec<T> manager);
    void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException;


    // to add a manager
    // sst. create the serializable manager
    // Zoom state
    // server Zoom state
    // Server state manipulator.updateUnit
    // client message handler
}
