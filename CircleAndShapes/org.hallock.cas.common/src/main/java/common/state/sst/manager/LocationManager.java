package common.state.sst.manager;

import common.state.spec.GameSpec;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.sst.GameState;
import common.util.*;
import common.util.json.JsonReaderWrapperSpec;
import common.util.json.JsonWriterWrapperSpec;
import common.util.json.ReadOptions;
import common.util.json.WriteOptions;
import common.util.query.EntityQueryFilter;
import common.util.query.NearestEntityQuery;
import common.util.query.NearestEntityQueryResults;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class LocationManager implements Serializable /* implements ManagerSpec<DPoint>*/ {

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

    public Set<EntityId> getEntitiesWithin(double gr1x, double gr1y, double gr2x, double gr2y, EntityQueryFilter filter) {
        synchronized (gridLocations) {
            return gridLocations.getEntitiesWithin(gr1x,  gr1y, gr2x, gr2y, filter);
        }
    }

    public Set<EntityId> getEntities(DPoint destination, EntityQueryFilter filter) {
        synchronized (gridLocations) {
            return gridLocations.getAt(destination, filter);
        }
    }

    public Set<EntityId> getEntities(Point destination, EntityQueryFilter filter) {
        synchronized (gridLocations) {
            return gridLocations.getAt(destination, filter);
        }
    }

    public DPoint getLocation(EntityId d) {
        synchronized (gridLocations) {
            return gridLocations.getLocation(d);
        }
    }

    public void setLocation(EntityReader entity, DPoint desiredLocation) {
        synchronized (gridLocations) {
            gridLocations.setLocation(entity, desiredLocation);
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

    public List<NearestEntityQueryResults> multiQuery(NearestEntityQuery query) {
        synchronized (gridLocations) {
            return gridLocations.multiQuery(query);
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

    public GameState.OccupancyView createOccupancyView(EntityId entityId) {
        return gridLocations.createOccupancyView(entityId);
    }
}
