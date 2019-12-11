package common.algo;

import common.state.spec.GameSpec;
import common.state.sst.OccupancyView;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class ConnectedSet {

    public static ArrayList<Point> getConnectedSet(Point start, OccupancyView occupancy, int maxSize) {
        ArrayList<Point> ret = new ArrayList<>();
        HashSet<Point> visited = new HashSet<>();
        LinkedList<Point> horizon = new LinkedList<>();
        horizon.add(start);
        while (ret.size() < maxSize && !horizon.isEmpty()) {
            Point current = horizon.removeFirst();
            if (occupancy.isOccupied(current.x, current.y)) {
                continue;
            }
            if (visited.contains(current)) {
                continue;
            }
            ret.add(current);
            visited.add(current);

            horizon.addLast(new Point(current.x - 1, current.y));
            horizon.addLast(new Point(current.x + 1, current.y));
            horizon.addLast(new Point(current.x, current.y + 1));
            horizon.addLast(new Point(current.x, current.y - 1));
            // diagonals?
        }
        return ret;
    }

    public static Point findNearestEmptyTile(GameSpec spec, Point location, OccupancyView view) {
        for (int r = 0; r < Math.max(spec.width, spec.height); r++) {
            for (int i = -r; i <= r; i++) {
                if (!view.isOccupied(location.x + r, location.y + i))
                    return new Point(location.x + r, location.y + i);
                if (!view.isOccupied(location.x - r, location.y + i))
                    return new Point(location.x - r, location.y + i);
                if (!view.isOccupied(location.x + i, location.y + r))
                    return new Point(location.x + i, location.y + r);
                if (!view.isOccupied(location.x + i, location.y - r))
                    return new Point(location.x + i, location.y - r);
            }
        }
        return null;
    }

    public static Point getRandomConnectedPoint(Random random, Point start, OccupancyView occupancy, int maxSize) {
        ArrayList<Point> connectedSet = getConnectedSet(start, occupancy, maxSize);
        if (connectedSet.isEmpty()) return null;
        return connectedSet.get(random.nextInt(connectedSet.size()));
    }
}
