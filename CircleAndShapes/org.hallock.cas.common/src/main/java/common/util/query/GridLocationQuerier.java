package common.util.query;

import common.algo.AStar;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.state.EntityId;
import common.state.Player;
import common.state.sst.GameState;
import common.util.DPoint;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class GridLocationQuerier {

    private static void checkNeighbor(GameState.OccupancyView occupancy, Set<DPoint> ret, Point tile, int dx, int dy) {
        if (occupancy.isOccupied(tile.x + dx, tile.y + dy)) {
            return;
        }
        ret.add(new DPoint(tile.x + dx, tile.y + dy));
    }

    public static Set<DPoint> enumerateNeighbors(DPoint location, Dimension size, GameState.OccupancyView occupancy) {
        // should be based on the size of the object too...
        Set<DPoint> ret = new HashSet<>();
        Point tile = location.toPoint();
        for (int dy = -1; dy <= size.height; dy++) {
            checkNeighbor(occupancy, ret, tile, 0, dy);
            checkNeighbor(occupancy, ret, tile, size.width, dy);
        }
        for (int dx = -1; dx <= size.width; dx++) {
            checkNeighbor(occupancy, ret, tile, dx, 0);
            checkNeighbor(occupancy, ret, tile, dx, size.height);
        }
        return ret;
    }

    public static final EntityQueryFilter ANY = entity -> true;

    public static EntityQueryFilter createNaturalResourceFilter(final GameState state, final String name) {
        return entity -> {
            EntitySpec type = state.typeManager.get(entity);
            return type.containsClass("natural-resource") && type.name.equals(name);
        };
    }

    public static AStar.PathSearch findPath(GameState state, DPoint location, DPoint destination, Player player) {
        AStar.PathSearch path = AStar.findPath(location, destination, state.getOccupancyView(player));
        if (!path.successful) {
            return null;
        }
        return path;
    }

    public static AStar.PathSearch findPath(GameState state, EntityId start, DPoint stop, Player player) {
        GameState.OccupancyView occupancy = state.getOccupancyView(player);

        EntitySpec type = state.typeManager.get(start);
        if (type == null) {
            return null;
        }

        DPoint startLocation = state.locationManager.getLocation(start);
        Set<DPoint> close = enumerateNeighbors(startLocation, type.size, occupancy);
        if (close.isEmpty()) {
            return null;
        }

        AStar.PathSearch path = AStar.findPath(close, stop, occupancy, null);
        if (!path.successful) {
            return null;
        }
        return path;
    }

    public static AStar.PathSearch findPath(GameState state, DPoint location, EntityId stop, Player player) {
        GameState.OccupancyView occupancy = state.getOccupancyView(player);

        EntitySpec type = state.typeManager.get(stop);
        if (type == null) {
            return null;
        }

        DPoint stopLocation = state.locationManager.getLocation(stop);
        Set<DPoint> close = enumerateNeighbors(stopLocation, type.size, occupancy);
        if (close.isEmpty()) {
            return null;
        }

        AStar.PathSearch path = AStar.findPath(location, close, occupancy);
        if (!path.successful) {
            return null;
        }
        return path;
    }

    public static AStar.PathSearch findPath(GameState state, EntityId start, EntityId stop, Player player) {
        return findPath(state, state.locationManager.getLocation(start), stop, player);
    }


//    static final class KeepSmallest<T> {
//
//        private final int desiredSize;
//        final LinkedList<T> list;
//        private final Comparator<T> comparator;
//
//
//        KeepSmallest(int size, Comparator<T> comparator) {
//            this.desiredSize = size;
//            this.list = new LinkedList<T>();
//            this.comparator = comparator;
//        }
//
//        public void add(T t) {
//            list.add(t);
//            Collections.sort(list, comparator);
//            while (list.size() > desiredSize) {
//                list.removeLast();
//            }
//        }
//    }
}
