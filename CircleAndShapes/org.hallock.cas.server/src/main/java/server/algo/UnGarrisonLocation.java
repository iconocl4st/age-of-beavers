package server.algo;

import common.factory.Path;
import common.factory.PathFinder;
import common.factory.SearchDestination;
import common.state.EntityReader;
import common.state.Occupancy;
import common.state.Player;
import common.state.spec.EntitySpec;
import common.state.sst.OccupancyView;
import common.util.DPoint;
import common.util.json.Jsonable;
import server.state.ServerGameState;

import java.awt.*;
import java.util.Set;

public final class UnGarrisonLocation {
    public final Point point;
    public final Path<? extends Jsonable> path;

    public UnGarrisonLocation(Point location, Path<? extends Jsonable> path) {
        this.point = location;
        this.path = path;
    }

    public static UnGarrisonLocation getUnGarrisonLocation(ServerGameState state, EntityReader holder, Dimension heldSize) {
        OccupancyView occView = Occupancy.createUnitOccupancy(state.state, holder.getOwner());
        EntitySpec type = holder.getType();
        DPoint location = holder.getLocation();
        DPoint gatherPoint = holder.getCurrentGatherPoint();
        if (gatherPoint != null) {
            Path<? extends Jsonable> path = state.pathFinder.findExitPath(occView, holder, heldSize, new SearchDestination(gatherPoint));
            if (path != null && !path.points.isEmpty())
                return new UnGarrisonLocation(path.points.get(0).toPoint(), path);
        }
        Set<Point> dPoints = PathFinder.enumerateNeighbors(location.toPoint(), type.size, occView);
        Point minimum = null;
        for (Point point : dPoints) {
            if (minimum == null || point.y < minimum.y)
                minimum = point;
            if (minimum == null || (point.x < minimum.x && point.y <= minimum.y))
                minimum = point;
        }
        return new UnGarrisonLocation(minimum, null);
    }

    public boolean isImpossible() {
        return this.point == null;
    }
}
