package common.util.query;

import common.factory.Path;
import common.factory.PathFinder;
import common.factory.SearchDestination;
import common.state.EntityReader;
import common.state.sst.GameState;
import common.util.DPoint;

public class NearestEntityQuery {
    public final int numToReturn;
    public final EntityReaderFilter filter;
    public final DPoint location;
    public final EntityReader entity;
    public final GameState state;
    public final double maxDistance;
    public final PathFinder pathFinder;

    public NearestEntityQuery(GameState state, DPoint startingLocation, EntityReaderFilter filter, double maxDistance, int numToReturn) {
        this.location = startingLocation;
        this.filter = filter;
        this.maxDistance = maxDistance;
        this.state = state;
        this.numToReturn = numToReturn;
        this.entity = null;
        this.pathFinder = null;
    }

    public NearestEntityQuery(EntityReader entity, EntityReaderFilter filter, PathFinder pathFinder, double maxDistance, int numToReturn) {
        this.location = entity.getLocation();
        this.entity = entity;
        this.filter = filter;
        this.maxDistance = maxDistance;
        this.state = entity.getState();
        this.numToReturn = numToReturn;
        this.pathFinder = pathFinder;
    }

    public Path findPath(EntityReader possible) {
        return pathFinder.findPath(entity, new SearchDestination(possible));
    }
}
