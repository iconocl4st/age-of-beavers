package common.util;

import common.factory.Path;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.sub.MovableEntity;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;
import common.util.query.EntityReaderFilter;
import common.util.query.KeepSmallest;
import common.util.query.NearestEntityQuery;
import common.util.query.NearestEntityQueryResults;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GridLocation {

    private final HashMap<EntityId, MovableEntity> locations = new HashMap<>();

    private final int width;

    private final int height;
    private final int numGridX;
    private final int numGridY;
    private final DirectedEntitySet[][] grid;


    public GridLocation(int width, int height, int numGridX, int numGridY) {
        this.width = width;
        this.height = height;
        this.numGridX = numGridX;
        this.numGridY = numGridY;

        grid = new DirectedEntitySet[numGridX][numGridY];
        for (int i = 0; i < numGridX; i++) {
            for (int j = 0; j < numGridY; j++) {
                grid[i][j] = new DirectedEntitySet();
            }
        }
    }

    public MovableEntity getDirectedEntity(EntityId entityId) {
        return locations.get(entityId);
    }

    public static class GraphicalDebugRectangle { public Rectangle2D rectangle; public String description; };

    public List<GraphicalDebugRectangle> getDebugRectangles() {
        double gridWidth = width / (double) numGridX;
        double gridHeight = height / (double) numGridY;
        List<GraphicalDebugRectangle> ret = new LinkedList<>();
        for (int i = 0; i < numGridX; i++) {
            for (int j = 0; j < numGridY; j++) {
                GraphicalDebugRectangle r = new GraphicalDebugRectangle();
                r.rectangle = new Rectangle2D.Double(
                    i * gridWidth,
                    j * gridHeight,
                    gridWidth,
                    gridHeight
                );
                r.description = "[" + i + "," + j + "] " + grid[i][j].size();
                ret.add(r);
            }
        }
        return ret;
    }
    public void clear() {
        for (DirectedEntitySet[] lists : grid) {
            for (DirectedEntitySet list : lists) {
                list.clear();
            }
        }
        locations.clear();
    }

    public void putAll(GridLocation otherLocations) {
        synchronized (locations) {
            locations.putAll(otherLocations.locations);
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    grid[i][j].addAll(otherLocations.grid[i][j]);
                }
            }
        }
    }

    private int getIndexX(double d) {
        return Math.min(numGridX - 1, Math.max(0, (int) (d * numGridX / (double) width)));
    }

    private int getIndexY(double d) {
        return Math.min(numGridY - 1, Math.max(0, (int) (d * numGridY / height)));
    }

    public Set<EntityReader> getEntitiesWithin(double gr1x, double gr1y, double gr2x, double gr2y, EntityReaderFilter filter) {
        Set<EntityReader> ret = new HashSet<>();
        int uppX = Math.min(numGridX - 1, getIndexX(gr2x));
        int uppY = Math.min(numGridY - 1, getIndexY(gr2y));
        for (int x = getIndexX(gr1x); x <= uppX; x++) {
            for (int y = getIndexY(gr1y); y <= uppY; y++) {
                synchronized (grid[x][y]) {
                    for (MovableEntity t : grid[x][y]) {
                        if (t.currentLocation.x > gr2x) continue;
                        if (t.currentLocation.x + t.size.width < gr1x) continue;
                        if (t.currentLocation.y > gr2y) continue;
                        if (t.currentLocation.y + t.size.height < gr1y) continue;
                        if (!filter.include(t.entity)) {
                            continue;
                        }
                        ret.add(t.entity);
                    }
                }
            }
        }
        return ret;
    }

    private Point getIndex(DPoint p) {
        return getIndex(p.toPoint());
    }

    private Point getIndex(Point p) {
        return getIndex(p.x, p.y);
    }


    public static Set<Point> getOverlappingTiles(DPoint p, Dimension size) {
        // seems to be missing the top part of buildings...
        Set<Point> ret = new HashSet<>();
        int x = (int) p.x;
        int y = (int) p.y;
        boolean dx = Math.abs(p.x - x) > 1e-4;
        boolean dy = Math.abs(p.y - y) > 1e-4;
        for (int i = 0; i < size.width; i++) {
            for (int j = 0; j < size.height; j++) {
                ret.add(new Point(x, y));
                if (dx) ret.add(new Point(x + 1, y));
                if (dy) ret.add(new Point(x, y + 1));
                if (dx && dy) ret.add(new Point(x + 1, y + 1));
            }
        }
        return ret;
    }

    private Point getIndex(int x, int y) {
        return new Point(getIndexX(x), getIndexY(y));
    }

    private void addToGrid(MovableEntity entity) {
        for (Point p : getOverlappingTiles(entity.currentLocation, entity.size)) {
            Point index = getIndex(p);
            synchronized (grid[index.x][index.y]) {
                grid[index.x][index.y].add(entity);
            }
        }
    }

    private void removeFromGrid(MovableEntity entity) {
        for (Point p : getOverlappingTiles(entity.currentLocation, entity.size)) {
            Point index = getIndex(p);
            synchronized (grid[index.x][index.y]) {
                grid[index.x][index.y].remove(entity);
            }
        }
    }

    public DPoint getLocation(EntityId entityId, double currentGameTime) {
        return getLocation(locations.get(entityId), currentGameTime);
    }
    public DPoint getLocation(MovableEntity entity, double currentGameTime) {
        if (entity == null)
            return null;
        if (Math.abs(entity.movementSpeed) < 1e-4)
            return entity.movementBegin;
        DPoint mb = entity.movementBegin;
        DPoint me = entity.movementEnd;

        double dx = me.x - mb.x;
        double dy = me.y - mb.y;
        double n = Math.sqrt(dx*dx+dy*dy);
        if (n < 1e-4)
            return entity.movementBegin;
        dx /= n;
        dy /= n;
        double dt = currentGameTime - entity.movementStartTime;
        double d = Math.min(entity.movementSpeed * dt, n);
        return new DPoint(
            mb.x + d * dx,
            mb.y + d * dy
        );
    }

    public void setTime(double currentGameTime) {
        for (MovableEntity entity : locations.values()) {
            updateCachedLocation(entity, getLocation(entity, currentGameTime));
        }
    }

    public void updateCachedLocation(MovableEntity previous, DPoint newCachedLocation) {
        if (newCachedLocation == null)
            throw new NullPointerException();
        // TODO synchronize...
        if (previous.currentLocation.equals(newCachedLocation))
            return;
        removeFromGrid(previous);
        previous.currentLocation = newCachedLocation;
        addToGrid(previous);
    }

    public void setLocation(MovableEntity entity) {
        if (entity.currentLocation == null || entity.movementEnd == null || entity.movementBegin == null)
            throw new NullPointerException();
        MovableEntity previous = locations.get(entity.entity.entityId);
        if (previous == null)
            add(entity);
        else
            directionChanged(previous, entity);
    }

    private void directionChanged(MovableEntity previous, MovableEntity current) {
        // TODO synchronize...
        removeFromGrid(previous);
        addToGrid(current);
        locations.put(current.entity.entityId, current);
    }

    public void add(MovableEntity entity) {
        // TODO synchronize...
        addToGrid(entity);
        locations.put(entity.entity.entityId, entity);
    }

    public void remove(EntityReader t) {
        // TODO synchronize...
        synchronized (locations) {
            MovableEntity cLoc = locations.remove(t.entityId);
            if (cLoc == null) return;
            removeFromGrid(cLoc);
        }
    }

    public Set<EntityReader> getAt(DPoint point, EntityReaderFilter filter) {
        Set<EntityReader> ret = new HashSet<>();
        Point index = getIndex(point);
        synchronized (grid[index.x][index.y]) {
            for (MovableEntity lc : grid[index.x][index.y]) {
                // todo: dry
                if (lc.currentLocation.x > point.x) continue;
                if (lc.currentLocation.y > point.y) continue;
                if (lc.currentLocation.x + lc.size.width < point.x) continue;
                if (lc.currentLocation.y + lc.size.height < point.y) continue;
                if (!filter.include(lc.entity))
                    continue;
                ret.add(lc.entity);
            }
        }
        return ret;
    }

    public Set<EntityReader> getAt(Point point, EntityReaderFilter filter) {
        Set<EntityReader> ret = new HashSet<>();
        Point index = getIndex(point);
        synchronized (grid[index.x][index.y]) {
            for (MovableEntity de : grid[index.x][index.y]) {
                if (!getOverlappingTiles(de.currentLocation, de.size).contains(point))
                    continue;
                if (!filter.include(de.entity))
                    continue;
                ret.add(de.entity);
            }
        }
        return ret;
    }

    public DPoint getLocation(EntityId t) {
        MovableEntity e = locations.get(t);
        if (e == null) return null;
        return e.currentLocation;
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        clear();

        reader.readBeginDocument();
        reader.readBeginArray("located-entities");
        while (reader.hasMoreInArray())
            add(reader.read(MovableEntity.Serializer, spec));
        reader.readEndArray();
        reader.readEndDocument();
    }



    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.writeBeginArray("located-entities");
        for (MovableEntity e : locations.values())
            writer.write(e, MovableEntity.Serializer, options);
        writer.writeEndArray();
        writer.writeEndDocument();
    }
























    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean isInGridBounds(int x, int y) {
        return 0 <= x && x < numGridX && 0 <= y && y < numGridY;
    }

    private double getMinDistanceFrom(DPoint location, int ndxX, int ndxY) {
        double gridWidth = width / (double) numGridX;
        double gridHeight = height / (double) numGridY;
        return Util.minimumDistanceToSquare(location.x, location.y,
                ndxX * gridWidth, (ndxX + 1) * gridWidth,
                ndxY * gridHeight, (ndxY + 1) * gridHeight
        );
    }

    private boolean shouldCheck(int x, int y, DPoint startingLocation, double maxDistance, double successfulDistance) {
        return isInGridBounds(x, y) &&
                getMinDistanceFrom(startingLocation, x, y) < maxDistance &&
                getMinDistanceFrom(startingLocation, x, y) < successfulDistance;
    }

    private static class UnPathedEntityQueryResults {

        EntityReader entity;
        DPoint location;
        double  distance;
        UnPathedEntityQueryResults(EntityReader entity, DPoint location, double distance) {
            this.entity = entity;
            this.location = location;
            this.distance = distance;
        }

    }
    public NearestEntityQueryResults query(NearestEntityQuery query) {
        if (query.numToReturn != 1) throw new IllegalArgumentException();
        List<NearestEntityQueryResults> nearestEntityQueryResults = multiQuery(query);
        if (nearestEntityQueryResults.isEmpty()) return new NearestEntityQueryResults(
                null,
                null,
                null,
                Double.MAX_VALUE
        );
        return nearestEntityQueryResults.iterator().next();
    }
    public List<NearestEntityQueryResults> multiQuery(NearestEntityQuery query) {
        KeepSmallest<NearestEntityQueryResults> smallest = new KeepSmallest<>(query.numToReturn);
        DPoint startingLocation = query.location;
        Point index = getIndex(startingLocation);
        for (int r = 0; r < Math.max(numGridX, numGridY); r++) {
            boolean tooFar = true;
            Set<Point> toSearch = new HashSet<>();
            for (int i = -r; i <= r; i++) {
                // adds four points twice
                if (shouldCheck(index.x + i, index.y - r, startingLocation, query.maxDistance, smallest.bound())) {
                    tooFar = false;
                    if (!grid[index.x + i][index.y - r].isEmpty())
                        toSearch.add(new Point(index.x + i, index.y - r));
                }
                if (shouldCheck(index.x + i, index.y + r, startingLocation, query.maxDistance, smallest.bound())) {
                    tooFar = false;
                    if (!grid[index.x + i][index.y + r].isEmpty())
                        toSearch.add(new Point(index.x + i, index.y + r));
                }
                if (shouldCheck(index.x - r, index.y + i, startingLocation, query.maxDistance, smallest.bound())) {
                    tooFar = false;
                    if (!grid[index.x - r][index.y + i].isEmpty())
                        toSearch.add(new Point(index.x - r, index.y + i));
                }
                if (shouldCheck(index.x + r, index.y + i, startingLocation, query.maxDistance, smallest.bound())) {
                    tooFar = false;
                    if (!grid[index.x + r][index.y + i].isEmpty())
                        toSearch.add(new Point(index.x + r, index.y + i));
                }
            }
            if (tooFar)
                break;

            TreeSet<UnPathedEntityQueryResults> ordered = new TreeSet<>(Comparator.comparingDouble(a -> a.distance));
            for (Point point : toSearch) {
                synchronized (grid[point.x][point.y]) {
                    for (MovableEntity lc : grid[point.x][point.y]) {
                        if (Util.minimumDistanceToSquare(
                                startingLocation.x,
                                startingLocation.y,
                                lc.currentLocation.x,
                                lc.currentLocation.x + lc.size.width,
                                lc.currentLocation.y,
                                lc.currentLocation.y + lc.size.height
                            ) > query.maxDistance) {
                            continue;
                        }
                        if (!query.filter.include(lc.entity)) {
                            continue;
                        }
                        ordered.add(new UnPathedEntityQueryResults(lc.entity, lc.currentLocation, startingLocation.distanceTo(lc.locationCenter())));
                    }
                }
            }
            for (UnPathedEntityQueryResults results : ordered) {
                Path path = null;
                if (query.pathFinder != null) {
                    Path pathSearch = query.findPath(results.entity);
                    if (!pathSearch.successful) {
                        continue;
                    }
                    path = pathSearch;
                }
                smallest.add(results.distance, new NearestEntityQueryResults(results.entity, results.location, path, results.distance));
            }
        }
        return smallest.toList();
    }




    private static final class DirectedEntitySet extends HashSet<MovableEntity> {}




//    public GameState.OccupancyView createOccupancyView(EntityId entityId) {
}
//        return (x, y) -> {
//            if (x < 0 || x >= width || y < 0 || y > height)
//                return true;
//            Set<EntityId> at = getAt(new Point(x, y), GridLocationQuerier.ANY);
//            if (at.isEmpty()) return false;
//            if (at.contains(entityId) && at.size() == 1) return false;
//            return true;
//        };
//    }
