package common;

import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.state.EntityId;
import common.state.sst.GameState;
import common.util.DPoint;

public class Proximity {

    /* TODO Remove this, maybe  have integer amounts of resources. */
    public static final double INTERACTION_DISTANCE = 1 + Math.pow(2.0, 0.5) + 0.2;



    public static boolean closeEnoughToInteract(EntityReader unit, EntityReader storage) {
        EntitySpec type1 = unit.getType();
        if (type1 == null) return false;
        DPoint location1 = unit.getLocation();
        if (location1 == null) return false;
        EntitySpec type2 = storage.getType();
        if (type2 == null) return false;
        DPoint location2 = storage.getLocation();
        if (location2 == null) return false;

        // todo: only consider the boundaries?
        for (int l1x = 0; l1x < type1.size.width; l1x++) {
            for (int l1y = 0; l1y < type1.size.height; l1y++) {
                for (int l2x = 0; l2x < type2.size.width; l2x++) {
                    for (int l2y = 0; l2y < type2.size.height; l2y++) {
                        double dx = location1.x + l1x - (location2.x + l2x);
                        double dy = location1.y + l1y - (location2.y + l2y);
                        if (Math.sqrt(dx*dx + dy*dy) < INTERACTION_DISTANCE) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
