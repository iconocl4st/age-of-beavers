package common.state;

import common.state.los.Exploration;
import common.state.sst.GameState;
import common.state.sst.OccupancyView;
import common.util.query.EntityReaderFilter;

import java.awt.*;

public class Occupancy {
    // could be based off the types, would probably be better...
    private static final EntityReaderFilter filter = e -> e.getGateState() == null && !e.isHidden() && e.getConstructionZone() == null;

    public static OccupancyView createConstructionOccupancy(GameState state, Exploration exploration) {
        return (x, y) ->
                state.staticOccupancy.isOutOfBounds(x, y) ||
                state.staticOccupancy.get(x, y) ||
                state.buildingOccupancy.get(x, y) ||
                !exploration.get(x, y) ||
                !state.gateStateManager.getByType(new Point(x, y)).isEmpty() // only works when gates have size 1...
        ;
    }
    public static OccupancyView createUnitOccupancy(EntityReader entity) {
        GameState state = entity.getState();
        Player owner = entity.getOwner();
        return (x, y) ->
                state.staticOccupancy.isOutOfBounds(x, y) ||
                state.staticOccupancy.get(x, y) ||
                !state.locationManager.getEntities(new Point(x, y), e -> !e.equals(entity) && filter.include(e)).isEmpty() /* ||
                GateInfo.isOccupiedFor(new Point(x, y), owner, state.gateStateManager, state.playerManager) */
        ;
    }

    public static OccupancyView createUnitOccupancy(GameState state, Player owner) {
        return (x, y) -> (
                state.staticOccupancy.isOutOfBounds(x, y) ||
                state.staticOccupancy.get(x, y) ||
                !state.locationManager.getEntities(new Point(x, y), filter).isEmpty() /* ||
                GateInfo.isOccupiedFor(new Point(x, y), owner, state.gateStateManager, state.playerManager) */
        );
    }
    public static OccupancyView createStaticOccupancy(GameState state, Player player) {
        return (x, y) ->
                state.staticOccupancy.isOutOfBounds(x, y) ||
                state.staticOccupancy.get(x, y) /* ||
                GateInfo.isOccupiedFor(new Point(x, y), player, state.gateStateManager, state.playerManager) */
        ;
    }

    public static OccupancyView createGenerationOccupancy(GameState state) {
        return (x, y) -> !state.locationManager.getEntities(new Point(x, y), EntityReaderFilter.Any).isEmpty();
    }

    public static boolean isOccupied(OccupancyView view, Point location, Dimension size) {
        return isOccupied(view, location.x,  location.y, size);
    }

    public static boolean isOccupied(OccupancyView view, int bx, int by, Dimension size) {
        for (int x = 0; x < size.width; x++)
            for (int y = 0; y < size.height; y++)
                if (view.isOccupied(bx + x, by + y))
                    return true;
        return false;
    }


//    public OccupancyView globalOccupancyView() {
//        return (x, y) -> isOutOfBounds(x, y) || isGloballyOccupied(x, y);
//    }

//    public Occupancy cloneFromPerspective(GateStateManager conditionalOccupancyManager, Player player, LineOfSightImpl los) {
//        int w = bitArray.getDimension(0);
//        int h = bitArray.getDimension(1);
//        Occupancy occ = new Occupancy(bitArray.getDimension(0), bitArray.getDimension(1));
//        for (int i = 0; i < w; i++) {
//            for (int j = 0; j < h; j++) {
//                if (!los.isVisible(null, i, j))
//                    continue;
//                occ.bitArray.set(i, j, 0, 0, bitArray.get(i, j, 0, 0));
//            }
//        }
//        return occ;
//    }
}
