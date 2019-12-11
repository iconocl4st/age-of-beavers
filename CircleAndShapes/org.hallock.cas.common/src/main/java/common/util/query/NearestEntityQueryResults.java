package common.util.query;

import common.factory.Path;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.GameState;
import common.util.DPoint;

public class NearestEntityQueryResults {
    public final Path path;
    public final DPoint location;
    public final EntityReader entity;
    public final double distance;

    public NearestEntityQueryResults(EntityReader entity, DPoint location, Path path, double distance) {
        this.entity = entity;
        this.path = path;
        this.distance = distance;
        this.location = location;
    }

    public boolean successful() {
        return entity != null;
    }
}
