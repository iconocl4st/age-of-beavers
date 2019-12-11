package common.factory;

import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.OccupancyView;
import common.util.Bounds;
import common.util.DPoint;
import common.util.GridLocation;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SearchDestination implements Jsonable {
    private final DPoint location;
    private final EntityReader entity;

    public SearchDestination(DPoint point) {
        this.location = point;
        this.entity = null;
    }

    public SearchDestination(EntityReader entity) {
        this.location = null;
        this.entity = entity;
    }

    public Set<Point> enumerateDestinationLocations(OccupancyView view) {
        Set<Point> ret = new HashSet<>();
        if (location == null) {
            DPoint loc = entity.getLocation();
            Dimension size = entity.getSize();
            if (loc == null || size == null)
                return ret;
            return PathFinder.enumerateNeighbors(loc.toPoint(), size, view);
        } else
            for (Point p : GridLocation.getOverlappingTiles(location, new Dimension(1, 1)))
                if (!view.isOccupied(p.x, p.y))
                    ret.add(p);
        return ret;
    }


    @Override
    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        writer.writeBeginDocument();
        writer.write("location", location, DPoint.Serializer, options);
        writer.write("entity", entity == null ? null :  entity.entityId, EntityId.Serializer, options);
        writer.writeEndDocument();
    }


    public static final DataSerializer<SearchDestination> Serializer = new DataSerializer.JsonableSerializer<SearchDestination>() {
        @Override
        public SearchDestination parse(JsonReaderWrapperSpec reader, ReadOptions opts) throws IOException {
            if (opts.state == null)
                throw new NullPointerException();
            reader.readBeginDocument();
            DPoint location = reader.read("location", DPoint.Serializer, opts);
            EntityReader entity = new EntityReader(opts.state, reader.read("entity", EntityId.Serializer, opts));
            reader.readEndDocument();

            if (location == null)
                return new SearchDestination(entity);
            else
                return new SearchDestination(location);
        }
    };

    public Path<? extends Jsonable> findPath(PathFinder pathFinder, EntityReader entity) {
        return pathFinder.findPath(entity, this);
    }

    public boolean isWithin(Bounds bounds) {
        if (location == null) {
            DPoint location = entity.getLocation();
            if (location == null) return false;
            return bounds.contains(location);
        } else {
            return bounds.contains(this.location);
        }
    }

    public Path<? extends Jsonable> findPath(PathFinder pathFinder, OccupancyView view, EntityReader entity, Bounds bounds) {
        return pathFinder.findPath(view, entity, this, bounds);
    }
}
