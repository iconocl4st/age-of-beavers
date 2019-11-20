package common.util.query;

import common.algo.AStar;
import common.state.EntityId;
import common.state.Player;
import common.state.sst.GameState;
import common.util.DPoint;

public class NearestEntityQuery {
    public final int numToReturn;
    public final EntityQueryFilter filter;
    public final DPoint location;
    public final GameState state;
    public final double maxDistance;
    public final Player playerPerspective;
    public final boolean needsPath;

    public NearestEntityQuery(GameState state, DPoint entity, EntityQueryFilter filter, double maxDistance, Player perspective) {
        this(state, entity, filter, maxDistance, perspective, 1);
    }

    public NearestEntityQuery(GameState state, DPoint entity, EntityQueryFilter filter, double maxDistance, Player perspective, int numToReturn) {
        this.location = entity;
        this.filter = filter;
        this.maxDistance = maxDistance;
        this.state = state;
        this.playerPerspective = perspective;
        needsPath = perspective != null;
        this.numToReturn = numToReturn;
    }

    public AStar.PathSearch findPath(DPoint location, EntityId entity) {
        return GridLocationQuerier.findPath(state, location, entity, state.playerManager.get(entity));
    }
}
