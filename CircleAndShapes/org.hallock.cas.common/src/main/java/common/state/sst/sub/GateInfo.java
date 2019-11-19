package common.state.sst.sub;

import common.state.EntityId;
import common.state.Player;
import common.state.sst.manager.ManagerImpl;
import common.state.sst.manager.RevPair;
import common.state.sst.manager.ReversableManagerImpl;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GateInfo implements Jsonable {
    public Point location;
    public GateState state;

    public GateInfo(Point point, GateState state) {
        this.location = point;
        this.state = state;
    }

    public boolean equals(Object other) {
        if (!(other instanceof GateInfo))
        return false;
        GateInfo o = (GateInfo) other;
        return o.location.equals(location) && state.equals(o.state);
    }

    public static Set<EntityId> getOccupancies(Player player, ReversableManagerImpl<GateInfo, Point> gateManager, ManagerImpl<Player> pManager) {
        Set<EntityId> ret = new HashSet<>();
        for (Map.Entry<EntityId, GateInfo> entry : gateManager.entrySet()) {
            if (!isOccupiedFor(entry.getKey(), entry.getValue(), player, pManager)) {
                continue;
            }
            ret.add(entry.getKey());
        }
        return ret;
    }

    public static boolean isOccupiedFor(EntityId entityId, Player player, ReversableManagerImpl<GateInfo, Point> gateManager, ManagerImpl<Player> pManager) {
        return isOccupiedFor(entityId, gateManager.get(entityId), player, pManager);
    }

    public static boolean isOccupiedFor(Point location, Player player, ReversableManagerImpl<GateInfo, Point> gateManager, ManagerImpl<Player> pManager) {
        Set<RevPair<GateInfo>> byType = gateManager.getByType(location);
        if (byType.isEmpty()) return false;
        if (byType.size() != 1) throw new RuntimeException("Two gates in the same spot?");
        EntityId entityId = byType.iterator().next().entityId;
        if (entityId == null) return false;
        return isOccupiedFor(entityId, player, gateManager, pManager);
    }

    public static boolean isOccupiedFor(EntityId entityId, GateInfo currentState, Player player, ManagerImpl<Player> pManager) {
        Player owner = pManager.get(entityId);
        switch (currentState.state) {
            case Unlocked:
                return false;
            case Locked:
                return true;
            case UnlockedForPlayerOnly:
                return !owner.equals(player);
            default:
                throw new IllegalStateException("Implement state " + currentState);
        }
    }


    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("location", location, DataSerializer.PointSerializer, options);
        writer.write("state",  state.ordinal());
        writer.writeEndDocument();
    }

    public static final DataSerializer<GateInfo> Serializer = new DataSerializer.JsonableSerializer<GateInfo>() {
        @Override
        public GateInfo parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
            reader.readBeginDocument();
            Point point = reader.read("location", DataSerializer.PointSerializer, spec);
            GateState state = reader.b(GateState.values(), reader.readInt32("state"));
            reader.readEndDocument();
            return new GateInfo(point, state);
        }
    };

    public enum GateState {
        Locked,
        Unlocked,
        UnlockedForPlayerOnly,
    }
}
