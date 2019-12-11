package common.state.sst.manager;

import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.state.sst.sub.MovableEntity;
import common.util.DPoint;
import common.util.GridLocation;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;
import common.util.query.EntityReaderFilter;
import common.util.query.NearestEntityQuery;
import common.util.query.NearestEntityQueryResults;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class LocationManager /* implements ManagerSpec<DPoint>*/ {

    // todo: remove
//    GameState gameState;

    // TODO Replace with r-tree
    // Maybe this one?
    // https://github.com/davidmoten/rtree


    // we are synchronizing twice...
    private final GridLocation gridLocations;

    public LocationManager(GameSpec spec) {
        gridLocations = new GridLocation(spec.width, spec.height, 20, 20);
    }

    public Set<EntityReader> getEntitiesWithin(double gr1x, double gr1y, double gr2x, double gr2y, EntityReaderFilter filter) {
        synchronized (gridLocations) {
            return gridLocations.getEntitiesWithin(gr1x,  gr1y, gr2x, gr2y, filter);
        }
    }

    public Set<EntityReader> getEntities(DPoint destination, EntityReaderFilter filter) {
        synchronized (gridLocations) {
            return gridLocations.getAt(destination, filter);
        }
    }

    public Set<EntityReader> getEntities(Point destination, EntityReaderFilter filter) {
        synchronized (gridLocations) {
            return gridLocations.getAt(destination, filter);
        }
    }

    public DPoint getLocation(EntityId d) {
        synchronized (gridLocations) {
            return gridLocations.getLocation(d);
        }
    }

    public void setLocation(MovableEntity entity) {
        synchronized (gridLocations) {
            gridLocations.setLocation(entity);
        }
    }

    public void updateAll(LocationManager locationManager) {
        synchronized (gridLocations) {
            gridLocations.clear();
            gridLocations.putAll(locationManager.gridLocations);
        }
    }

    public void remove(EntityReader entity) {
        synchronized (gridLocations) {
            gridLocations.remove(entity);
        }
    }

    public NearestEntityQueryResults query(NearestEntityQuery query) {
        synchronized (gridLocations) {
            return gridLocations.query(query);
        }
    }

    public List<GridLocation.GraphicalDebugRectangle> getDebugRectangles() {
        return gridLocations.getDebugRectangles();
    }

    public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
        synchronized (gridLocations) {
            gridLocations.writeTo(writer, options);
        }
    }

    public void updateAll(JsonReaderWrapperSpec reader, ReadOptions gameSpec) throws IOException {
        synchronized (gridLocations) {
            gridLocations.updateAll(reader, gameSpec);
        }
    }

    public MovableEntity getDirectedEntity(EntityId entityId) {
        synchronized (gridLocations) {
            return gridLocations.getDirectedEntity(entityId);
        }
    }

    public void updateCachedLocation(EntityReader entity, DPoint cachedCurrentLocation) {
        synchronized (gridLocations) {
            MovableEntity directedEntity = gridLocations.getDirectedEntity(entity.entityId);
            if (directedEntity == null) return;
            gridLocations.updateCachedLocation(directedEntity, cachedCurrentLocation);
        }
    }

    public void setTime(double currentGameTime) {
        synchronized (gridLocations) {
            gridLocations.setTime(currentGameTime);
        }
    }

    public DPoint getLocation(EntityId entityId, double currentGameTime) {
        synchronized (gridLocations) {
            return gridLocations.getLocation(entityId, currentGameTime);
        }
    }

//    public OccupancyView getOccupancyView(EntityReader entity) {
//        synchronized (gridLocations) {
//            return gridLocations.createUnitOccupancyView(entity);
//        }
//    }
}
