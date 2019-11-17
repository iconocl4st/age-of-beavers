package common.state.los;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public final class VisibilityChange {
    public final Set<Point> gainedVision = new HashSet<>();
    public final Set<Point> lostVision = new HashSet<>();
    public final Set<Point> becameExplored = new HashSet<>();

    public void becameExplored(int x, int y) {
        becameExplored.add(new Point(x, y));
    }

    public void addVisibility(int x, int y) {
        Point p = new Point(x, y);
        if (lostVision.contains(p))
            lostVision.remove(p);
        else
            gainedVision.add(p);
    }

    public void removeVisibility(int x, int y) {
        Point p = new Point(x, y);
        if (gainedVision.contains(p))
            gainedVision.remove(p);
        else
            lostVision.add(p);
    }
}
