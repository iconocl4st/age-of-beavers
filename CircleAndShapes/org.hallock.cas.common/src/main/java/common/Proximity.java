package common;

import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.util.DPoint;

import java.awt.*;

public class Proximity {

    /* TODO Remove this, maybe  have integer amounts of resources. */
    public static final double INTERACTION_DISTANCE = /*1 + Math.pow(2.0, 0.5) + */ 0.2;



    public static boolean closeEnoughToInteract(EntityReader unit, EntityReader storage) {
        if (unit == null || storage == null) return false;
        Dimension size1 = unit.getSize();
        if (size1 == null) return false;
        DPoint location1 = unit.getLocation();
        if (location1 == null) return false;
        Dimension size2 = storage.getSize();
        if (size2 == null) return false;
        DPoint location2 = storage.getLocation();
        if (location2 == null) return false;

        double closestUnitX = Math.min(location1.x + size1.width, Math.max(location1.x, location2.x));
        double closestUnitY = Math.min(location1.y + size1.height, Math.max(location1.y, location2.y));

        double closestStorageX = Math.min(location2.x + size2.width, Math.max(location2.x, closestUnitX));
        double closestStorageY = Math.min(location2.y + size2.height, Math.max(location2.y, closestUnitY));

        double dx = closestUnitX - closestStorageX;
        double dy = closestUnitY - closestStorageY;

        double d = Math.sqrt(dx*dx+dy*dy);
        return d < INTERACTION_DISTANCE;
    }
}
