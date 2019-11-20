package common.util.query;

import common.algo.AStar;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.GameState;
import common.util.DPoint;

public class NearestEntityQueryResults {
    public final AStar.Path path;
    public final DPoint location;
    public final EntityId entityId;
    public final double distance;

    public NearestEntityQueryResults(EntityId entityIds, DPoint location, AStar.Path path, double distance) {
        this.entityId = entityIds;
        this.path = path;
        this.distance = distance;
        this.location = location;
    }

    public boolean successful() {
        return entityId != null;
    }

    public EntityReader getEntity(GameState state) {
        if (entityId == null) return null;
        return new EntityReader(state, entityId);
    }
}
