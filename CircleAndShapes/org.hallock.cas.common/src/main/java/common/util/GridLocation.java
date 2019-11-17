package common.util;

import common.algo.AStar;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.LocatedEntitySpec;
import common.state.sst.GameState;
import common.util.json.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class GridLocation implements Serializable {

    public GameState.OccupancyView createOccupancyView(EntityId entityId) {
        return (x, y) -> {
            if (x < 0 || x >= width || y < 0 || y > height)
                return true;
            Set<EntityId> at = getAt(new Point(x, y), GridLocationQuerier.ANY);
            if (at.isEmpty()) return false;
            if (at.contains(entityId) && at.size() == 1) return false;
            return true;
        };
    }

    private static final class ListOfEntityIds implements Serializable {
        Set<LocatedEntity> entityIds = new HashSet<>();

        public void clear() {
            entityIds.clear();
        }

        public void add(EntityId t, DPoint location, Dimension size) {
            LocatedEntity lc = new LocatedEntity();
            lc.entityId = t;
            lc.location = location;
            lc.size = size;
            entityIds.add(lc);
        }

        public void remove(final EntityId t) {
            entityIds.removeIf(e -> e.entityId.equals(t));
        }

        public void setLocation(EntityId t, DPoint point, Dimension size) {
            for (LocatedEntity lc : entityIds) {
                if (lc.entityId.equals(t)) {
                    lc.location = point;
                    lc.size = size;
                    return;
                }
            }
            throw new RuntimeException("Was supposed to be here, but it wasn't found.");
        }
    }

    private static final class LocatedEntity implements Serializable {
        EntityId entityId;
        DPoint location;
        Dimension size;

        public DPoint locationCenter() {
            return new DPoint(location.x + size.width / 2.0, location.y + size.height / 2.0);
        }

        public boolean equals(Object other) {
            return other instanceof LocatedEntity && ((LocatedEntity) other).entityId.equals(entityId);
        }

        public int hashCode() {
            return entityId.hashCode();
        }
    }

    private final HashMap<EntityId, DPoint> locations = new HashMap<>();

    private final int width;
    private final int height;
    private final int numGridX;
    private final int numGridY;

    private final ListOfEntityIds[][] grid;


    public GridLocation(int width, int height, int numGridX, int numGridY) {
        this.width = width;
        this.height = height;
        this.numGridX = numGridX;
        this.numGridY = numGridY;

        grid = new ListOfEntityIds[numGridX][numGridY];
        for (int i = 0; i < numGridX; i++) {
            for (int j = 0; j < numGridY; j++) {
                grid[i][j] = new ListOfEntityIds();
            }
        }
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
                r.description = "[" + i + "," + j + "] " + grid[i][j].entityIds.size();
                ret.add(r);
            }
        }
        return ret;
    }

    public void clear() {
        for (ListOfEntityIds[] lists : grid) {
            for (ListOfEntityIds list : lists) {
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
                    grid[i][j].entityIds.addAll(otherLocations.grid[i][j].entityIds);
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

    public Set<EntityId> getEntitiesWithin(double gr1x, double gr1y, double gr2x, double gr2y, EntityQueryFilter filter) {
        Set<EntityId> ret = new HashSet<>();
        int uppX = Math.min(numGridX - 1, getIndexX(gr2x));
        int uppY = Math.min(numGridY - 1, getIndexY(gr2y));
        for (int x = getIndexX(gr1x); x <= uppX; x++) {
            for (int y = getIndexY(gr1y); y <= uppY; y++) {
                synchronized (grid[x][y]) {
                    for (LocatedEntity t : grid[x][y].entityIds) {
                        if (t.location.x > gr2x) continue;
                        if (t.location.x + t.size.width < gr1x) continue;
                        if (t.location.y > gr2y) continue;
                        if (t.location.y + t.size.height < gr1y) continue;
                        if (!filter.include(t.entityId)) {
                            continue;
                        }
                        ret.add(t.entityId);
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
    private Point getIndex(int x, int y) {
        return new Point(getIndexX(x), getIndexY(y));
    }

    private void addToGrid(EntityId t, DPoint location, Dimension size) {
        for (int i = 0; i < size.width; i++) {
            for (int j = 0; j < size.height; j++) {
                Point index = getIndex((int) (location.x + i), (int) (location.y + j));
                synchronized (grid[index.x][index.y]) {
                    grid[index.x][index.y].add(t, location, size);
                }
            }
        }
    }

    private void removeFromGrid(EntityId t, Point location, Dimension size) {
        for (int i = 0; i < size.width; i++) {
            for (int j = 0; j < size.height; j++) {
                Point index = getIndex(location.x + i, location.y + j);
                synchronized (grid[index.x][index.y]) {
                    grid[index.x][index.y].remove(t);
                }
            }
        }
    }

    public void move(LocatedEntitySpec t, DPoint point) {
        // TODO synchronize...
        Dimension size = t.getSize();
        removeFromGrid(t.getEntityId(), locations.get(t.getEntityId()).toPoint(), size);
        addToGrid(t.getEntityId(), point, size);
        locations.put(t.getEntityId(), point);
    }

    public void add(LocatedEntitySpec t, DPoint location) {
        // TODO synchronize...
        addToGrid(t.getEntityId(), location, t.getSize());
        locations.put(t.getEntityId(), location);
    }

    public void remove(EntityReader t) {
        // TODO synchronize...
        synchronized (locations) {
            DPoint cLoc = locations.remove(t.entityId);
            if (cLoc == null) return;
            removeFromGrid(t.entityId, cLoc.toPoint(), t.getType().size);
        }
    }


    public Set<EntityId> getAt(DPoint point, EntityQueryFilter filter) {
        Set<EntityId> ret = new HashSet<>();
        Point index = getIndex(point);
        synchronized (grid[index.x][index.y]) {
            for (LocatedEntity lc : grid[index.x][index.y].entityIds) {
                // todo: dry
                if (lc.location.x > point.x) continue;
                if (lc.location.y > point.y) continue;
                if (lc.location.x + lc.size.width < point.x) continue;
                if (lc.location.y + lc.size.height < point.y) continue;
                if (!filter.include(lc.entityId))
                    continue;
                ret.add(lc.entityId);
            }
        }
        return ret;
    }

    public Set<EntityId> getAt(Point point, EntityQueryFilter filter) {
        Set<EntityId> ret = new HashSet<>();
        Point index = getIndex(point);
        synchronized (grid[index.x][index.y]) {
            for (LocatedEntity id : grid[index.x][index.y].entityIds) {
                if (!id.location.toPoint().equals(point))
                    continue;
                if (!filter.include(id.entityId))
                    continue;
                ret.add(id.entityId);
            }
        }
        return ret;
    }

    public DPoint getLocation(EntityId t) {
        return locations.get(t);
    }

    public void setLocation(LocatedEntitySpec entity, DPoint desiredLocation) {
        if (locations.containsKey(entity.getEntityId())) {
            move(entity, desiredLocation);
        } else {
            add(entity, desiredLocation);
        }
    }













    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
        reader.readBeginDocument();
        reader.readBeginArray("located-entities");
        while (reader.hasMoreInArray()) {
            reader.readBeginDocument();
            EntityId entity = reader.read("entity", EntityId.Serializer, spec);
            DPoint location = reader.read("location", DPoint.Serializer, spec);
            Dimension size = reader.read("size", DataSerializer.DimensionSerializer, spec);
            add(new LocatedEntitySpec() {
                @Override
                public EntityId getEntityId() {
                    return entity;
                }

                @Override
                public DPoint getLocation() {
                    return location;
                }

                @Override
                public Dimension getSize() {
                    return size;
                }
            }, location);
            reader.readEndDocument();
        }
        reader.readEndArray();
        reader.readEndDocument();
    }



    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        HashSet<LocatedEntity> alreadyWritten = new HashSet<>();

        writer.writeBeginDocument();
        writer.writeBeginArray("located-entities");
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                for (LocatedEntity entity : grid[i][j].entityIds) {
                    if (!alreadyWritten.add(entity))
                        continue;
                    writer.writeBeginDocument();
                    writer.write("entity", entity.entityId, EntityId.Serializer, options);
                    writer.write("location", entity.location, DPoint.Serializer, options);
                    writer.write("size", entity.size, DataSerializer.DimensionSerializer, options);
                    writer.writeEndDocument();
                }
            }
        }
        writer.writeEndArray();
        writer.writeEndDocument();
    }























    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isInGridBounds(int x, int y) {
        return 0 <= x && x < numGridX && 0 <= y && y < numGridY;
    }

    private double minimumDistanceToSquare(double x, double y, double xmin, double xmax, double ymin, double ymax) {
        double dx = x - Math.min(xmax, Math.max(xmin, x));
        double dy = y - Math.min(ymax, Math.max(ymin, y));
        return Math.sqrt(dx*dx + dy*dy);
    }

    private double getMinDistanceFrom(DPoint location, int ndxX, int ndxY) {
        double gridWidth = width / (double) numGridX;
        double gridHeight = height / (double) numGridY;
        return minimumDistanceToSquare(location.x, location.y,
                ndxX * gridWidth, (ndxX + 1) * gridWidth,
                ndxY * gridHeight, (ndxY + 1) * gridHeight
        );
    }

    private boolean shouldCheck(int x, int y, DPoint startingLocation, double maxDistance, double successfulDistance) {
        return isInGridBounds(x, y) &&
                getMinDistanceFrom(startingLocation, x, y) < maxDistance &&
                getMinDistanceFrom(startingLocation, x, y) < successfulDistance;
    }

    public GridLocationQuerier.NearestEntityQueryResults query(GridLocationQuerier.NearestEntityQuery query) {
        GridLocationQuerier.NearestEntityQueryResults failed = new GridLocationQuerier.NearestEntityQueryResults(null, null, null, Double.MAX_VALUE);
        DPoint startingLocation = query.location;
        Point index = getIndex(startingLocation);
        GridLocationQuerier.NearestEntityQueryResults best = failed;
        for (int r = 0; r < Math.max(numGridX, numGridY); r++) {
            boolean tooFar = true;
            Set<Point> toSearch = new HashSet<>();
            for (int i = -r; i <= r; i++) {
                // adds four points twice
                if (shouldCheck(index.x + i, index.y - r, startingLocation, query.maxDistance, best.distance)) {
                    tooFar = false;
                    if (!grid[index.x + i][index.y - r].entityIds.isEmpty())
                        toSearch.add(new Point(index.x + i, index.y - r));
                }
                if (shouldCheck(index.x + i, index.y + r, startingLocation, query.maxDistance, best.distance)) {
                    tooFar = false;
                    if (!grid[index.x + i][index.y + r].entityIds.isEmpty())
                        toSearch.add(new Point(index.x + i, index.y + r));
                }
                if (shouldCheck(index.x - r, index.y + i, startingLocation, query.maxDistance, best.distance)) {
                    tooFar = false;
                    if (!grid[index.x - r][index.y + i].entityIds.isEmpty())
                        toSearch.add(new Point(index.x - r, index.y + i));
                }
                if (shouldCheck(index.x + r, index.y + i, startingLocation, query.maxDistance, best.distance)) {
                    tooFar = false;
                    if (!grid[index.x + r][index.y + i].entityIds.isEmpty())
                        toSearch.add(new Point(index.x + r, index.y + i));
                }
            }
            if (tooFar)
                return best;

            TreeSet<GridLocationQuerier.NearestEntityQueryResults> ordered = new TreeSet<>(Comparator.comparingDouble(a -> a.distance));
            for (Point point : toSearch) {
                synchronized (grid[point.x][point.y]) {
                    for (LocatedEntity lc : grid[point.x][point.y].entityIds) {
                        if (minimumDistanceToSquare(
                                startingLocation.x,
                                startingLocation.y,
                                lc.location.x,
                                lc.location.x + lc.size.width,
                                lc.location.y,
                                lc.location.y + lc.size.height
                            ) > query.maxDistance) {
                            continue;
                        }
                        if (!query.filter.include(lc.entityId)) {
                            continue;
                        }
                        ordered.add(new GridLocationQuerier.NearestEntityQueryResults(lc.entityId, lc.location, null, startingLocation.distanceTo(lc.locationCenter())));
                    }
                }
            }
            for (GridLocationQuerier.NearestEntityQueryResults results : ordered) {
                AStar.Path path = null;
                if (query.needsPath) {
                    AStar.PathSearch pathSearch = query.findPath(startingLocation, results.entity);
                    if (pathSearch == null) {
                        continue;
                    }
                    path = pathSearch.path;
                }
                best = new GridLocationQuerier.NearestEntityQueryResults(results.entity, results.location, path, results.distance);
                break;
            }
        }
        return failed;
    }
}
