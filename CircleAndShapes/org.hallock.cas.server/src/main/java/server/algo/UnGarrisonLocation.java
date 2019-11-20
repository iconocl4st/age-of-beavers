package server.algo;

import common.algo.AStar;
import common.state.EntityReader;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.query.GridLocationQuerier;

import java.util.Set;

public final class UnGarrisonLocation {
    public final DPoint point;
    public final AStar.Path path;

    public UnGarrisonLocation(DPoint location, AStar.Path path) {
        this.point = location;
        this.path = path;
    }

    public static UnGarrisonLocation getUnGarrisonLocation(GameState state, EntityReader holder) {
        Player owner = holder.getOwner();
        GameState.OccupancyView occupancyState = state.getOccupancyView(owner);
        EntitySpec type = holder.getType();
        DPoint location = holder.getLocation();
        DPoint gatherPoint = holder.getCurrentGatherPoint();

        if (gatherPoint != null) {
            AStar.PathSearch path = GridLocationQuerier.findPath(state, holder.entityId, gatherPoint, owner);
            if (path != null) {
                return new UnGarrisonLocation(path.path.points.get(0), path.path);
            }
        }
        Set<DPoint> dPoints = GridLocationQuerier.enumerateNeighbors(location, type.size, occupancyState);

        DPoint minimum = null;
        for (DPoint point : dPoints) {
            if (minimum == null || point.y < minimum.y) {
                minimum = point;
            }
            if (minimum == null || (point.x < minimum.x && point.y <= minimum.y)) {
                minimum = point;
            }
        }
        return new UnGarrisonLocation(minimum, null);
    }

    public boolean isImossible() {
        return this.point == null;
    }
}
