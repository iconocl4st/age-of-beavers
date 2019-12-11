package common.factory;

import common.algo.AStar;
import common.algo.jmp_pnt.JumpPointSearch;
import common.algo.quad.QuadTree;
import common.algo.quad.RootFinder;
import common.state.EntityReader;
import common.state.Occupancy;
import common.state.spec.EntitySpec;
import common.state.spec.GameSpec;
import common.state.sst.OccupancyView;
import common.util.Bounds;
import common.util.DPoint;
import common.util.GridLocation;
import common.util.json.Jsonable;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public abstract class PathFinder {

    private RootFinder rootFinder;
    private QuadTree tree;

    private static void checkNeighbor(OccupancyView occupancy, Set<Point> ret, Point tile, int dx, int dy) {
        if (occupancy.isOccupied(tile.x + dx, tile.y + dy)) {
            return;
        }
        ret.add(new Point(tile.x + dx, tile.y + dy));
    }

    public static Set<Point> enumerateNeighbors(Point tile, Dimension size, OccupancyView occupancy) {
        Set<Point> ret = new HashSet<>();
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

    private static Set<Point> enumerateNeighbors(Point tile, OccupancyView occupancy) {
        Set<Point> ret = new HashSet<>();
        checkNeighbor(occupancy, ret, tile, 1, 0);
        checkNeighbor(occupancy, ret, tile, 0, 1);
        checkNeighbor(occupancy, ret, tile, -1, 0);
        checkNeighbor(occupancy, ret, tile, 0, -1);

        if (!occupancy.isOccupied(tile.x, tile.y + 1) && !occupancy.isOccupied(tile.x + 1, tile.y + 1))
            checkNeighbor(occupancy, ret, tile, 1, 1);
        if (!occupancy.isOccupied(tile.x, tile.y + 1) && !occupancy.isOccupied(tile.x - 1, tile.y + 1))
            checkNeighbor(occupancy, ret, tile, -1, 1);
        if (!occupancy.isOccupied(tile.x, tile.y - 1) && !occupancy.isOccupied(tile.x + 1, tile.y - 1))
            checkNeighbor(occupancy, ret, tile, 1, -1);
        if (!occupancy.isOccupied(tile.x, tile.y - 1) && !occupancy.isOccupied(tile.x - 1, tile.y - 1))
            checkNeighbor(occupancy, ret, tile, -1, -1);
        return ret;
    }

    public abstract Path<? extends Jsonable> findPathImpl(Set<Point> begins, Set<Point> ends, Dimension size, OccupancyView view, Bounds bounds, SearchDestination destination);


    private Set<Point> tryToFindConnectedEnds(RootFinder rf, Set<Point> begins, Set<Point> ends) {
        if (rf == null) return ends;
        begins.removeIf(p -> rf.getRoot(p.x, p.y) < 0);
        HashSet<Point> connectedEnds = new HashSet<>();
        for (Point e : ends) {
            int er = rf.getRoot(e.x, e.y);
            if (er < 0)
                continue;
            for (Point b : begins) {
                int br = rf.getRoot(b.x, b.y);
                if (br < 0)
                    continue;
                if (br != er)
                    continue;
                connectedEnds.add(e);
                break;
            }
        }
        if (connectedEnds.isEmpty())
            for (Point b : begins) {
                for (Point e : ends) {
                    Point point = tree.nearestConnected(b, e);
                    if (point == null) continue;
                    connectedEnds.add(point);
                    break;
                }
            }
        if (connectedEnds.isEmpty())
            return ends;
        return connectedEnds;
    }

    public Path<? extends Jsonable> findPath(OccupancyView view, Set<Point> begins, Set<Point> ends, Dimension size, Bounds bounds, SearchDestination destination) {
        ends = tryToFindConnectedEnds(this.rootFinder, begins, ends);
        if (ends.isEmpty()) return Path.FAILED;
        return findPathImpl(begins, ends, size, view, bounds, destination);
    }

//    private Path findSimplePath(DPoint start, DPoint end) {
//        Path points = new Path(true);
//        points.points.addLast(start);
//        points.points.addLast(end);
//        return points;
//    }


    public Path<? extends Jsonable> findExitPath(OccupancyView view, EntityReader garrisoner, Dimension garrisonedSize, SearchDestination destination) {
        DPoint gLocation = garrisoner.getLocation();
        if (gLocation == null) return Path.FAILED;
        Point location = gLocation.toPoint();
        Path<? extends Jsonable> path = findPath(
                view,
                enumerateNeighbors(location, garrisonedSize, view),
                destination.enumerateDestinationLocations(view),
                garrisonedSize,
                Bounds.None,
                destination
        );
//        path.points.addLast(destination);
        return path;
    }

    public Path<? extends Jsonable> findPath(EntityReader entity, SearchDestination destination) {
        return findPath(entity, destination, Bounds.None);
    }
    public Path<? extends Jsonable> findPath(EntityReader entity, SearchDestination destination, Bounds bounds) {
        return findPath(Occupancy.createStaticOccupancy(entity.getState(), entity.getOwner()), entity, destination, bounds);
    }
    public Path<? extends Jsonable> findPath(OccupancyView view, EntityReader entity, SearchDestination destination, Bounds bounds) {
        Dimension size = entity.getSize();
        if (size == null) return Path.FAILED;
        DPoint location = entity.getLocation();
        if (location == null) return Path.FAILED;
        Path<? extends Jsonable> path = findPath(
                view,
                GridLocation.getOverlappingTiles(location, size),
                destination.enumerateDestinationLocations(view),
                size,
                bounds,
                destination
        );
        return path;
    }

    private Set<Point> singleton(Point point) {
        Set<Point> r = new HashSet<>();
        r.add(point);
        return r;
    }


    public static final String ASTAR_SEARCH = "astar";
    public static final String JUMP_STAR_SEARCH = "jps";
    public static final String CURRENT_SEARCH = JUMP_STAR_SEARCH;

    public static PathFinder createPathFinder(GameSpec gameSpec, String name) {
        switch (name) {
            case ASTAR_SEARCH:
                return new PathFinder() {
                    @Override
                    public Path findPathImpl(Set<Point> begins, Set<Point> ends, Dimension size, OccupancyView view, Bounds bounds, SearchDestination destination) {
                        return AStar.aStarSearch(begins, ends, view, bounds, destination);
                    }
                };
            case JUMP_STAR_SEARCH:
                return new PathFinder() {
                    @Override
                    public Path findPathImpl(Set<Point> begins, Set<Point> ends, Dimension size, OccupancyView view, Bounds bounds, SearchDestination destination) {
                        return JumpPointSearch.search(view, gameSpec.width, gameSpec.height, begins, ends, new HashSet<>(), bounds, destination);
                    }
                };
            default:
                throw new IllegalStateException(name);
        }
    }

    public void setRootFinder(QuadTree tree, RootFinder rf) {
        this.tree = tree;
        rootFinder = rf;
    }
}
