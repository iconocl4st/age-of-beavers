package server.algo;

import common.algo.AStar;
import common.state.EntityId;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.sst.GameState;
import common.util.DPoint;
import common.util.GridLocationQuerier;

import java.util.Set;

public final class UnGarrisonLocation {
    public final DPoint point;
    public final AStar.Path path;

    public UnGarrisonLocation(DPoint location, AStar.Path path) {
        this.point = location;
        this.path = path;
    }

    public static UnGarrisonLocation getUnGarrisonLocation(GameState state, EntityId holder) {
        Player owner = state.playerManager.get(holder);
        GameState.OccupancyView occupancyState = state.getOccupancyView(owner);
        EntitySpec type = state.typeManager.get(holder);
        DPoint location = state.locationManager.getLocation(holder);
        DPoint gatherPoint = state.gatherPointManager.get(holder);

        if (gatherPoint != null) {
            AStar.PathSearch path = GridLocationQuerier.findPath(state, holder, gatherPoint, owner);
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
