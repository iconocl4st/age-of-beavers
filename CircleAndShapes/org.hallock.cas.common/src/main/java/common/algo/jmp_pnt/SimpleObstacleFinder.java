package common.algo.jmp_pnt;

public class SimpleObstacleFinder implements ObstacleFinder {
    private static JpsNode searchH(JpsNode p, JumpPointContext c, int x, int y, int dx) {
        int d = 1;
        while (true) {
            final int cx = x + d * dx;
            if (c.bounds.outOfBoundsX(cx) || c.view.isOccupied(cx, y)) {
                c.append(cx, y, "b");
                return ObstacleFinder.BLOCKED;
            }
            if (c.goals.contains(cx * c.h + y)) {
                c.append(cx, y, "g");
                return new JpsNode(p, cx, y, ObstacleType.GOAL);
            }
            boolean fnu = y + 1 < c.bounds.ymax && c.view.isOccupied(cx - dx, y + 1) && !c.view.isOccupied(cx, y + 1);
            boolean fnd = y - 1 >= c.bounds.ymin && c.view.isOccupied(cx - dx, y - 1) && !c.view.isOccupied(cx, y - 1);
            if (fnu || fnd) {
                c.append(cx, y, (fnu ? "fnu" : "") + (fnd ? "fnd" : ""));
            }
            if (fnu) {
                if (fnd) return new JpsNode(p, cx, y, ObstacleType.FORCED_NEIGHBOR_UP, ObstacleType.FORCED_NEIGHBOR_DOWN);
                else return new JpsNode(p, cx, y, ObstacleType.FORCED_NEIGHBOR_UP);
            } else if (fnd) return new JpsNode(p, cx, y, ObstacleType.FORCED_NEIGHBOR_DOWN);
            ++d;
        }
    }

    private static JpsNode searchV(JpsNode p, JumpPointContext c, int x, int y, int dy) {
        int d = 1;
        while (true) {
            final int cy = y + d * dy;
            if (c.bounds.outOfBoundsY(cy) || c.view.isOccupied(x, cy)) {
                c.append(x, cy, "b");
                return ObstacleFinder.BLOCKED;
            }
            if (c.goals.contains(x * c.h + cy)) {
                c.append(x, cy, "g");
                return new JpsNode(p, x, cy, ObstacleType.GOAL);
            }
            boolean fnr = x + 1 < c.bounds.xmax && c.view.isOccupied(x + 1, cy - dy) && !c.view.isOccupied(x + 1, cy);
            boolean fnl = x - 1 >= c.bounds.xmin && c.view.isOccupied(x - 1, cy - dy) && !c.view.isOccupied(x - 1, cy);
            if (fnr || fnl) {
                c.append(x, cy, (fnr ? "fnr" : "") + (fnl ? "fnl" : ""));
            }
            if (fnr) {
                if (fnl) return new JpsNode(p, x, cy, ObstacleType.FORCED_NEIGHBOR_RIGHT, ObstacleType.FORCED_NEIGHBOR_LEFT);
                else return new JpsNode(p, x, cy, ObstacleType.FORCED_NEIGHBOR_RIGHT);
            } else if (fnl) return new JpsNode(p, x, cy, ObstacleType.FORCED_NEIGHBOR_LEFT);
            ++d;
        }
    }

    private static JpsNode searchD(JpsNode p, JumpPointContext c, int x, int y, int dx, int dy) {
        int d = 1;
        while (true) {
            int cx = x + d * dx;
            int cy = y + d * dy;
            if (c.bounds.outOfBounds(cx, cy) || c.view.isOccupied(cx, cy)) {
                if (false /*d > 1*/)
                    return new JpsNode(p, cx, cy, ObstacleType.FORCED_NEIGHBOR_DIAG);
                else {
                    c.append(cx, cy, "b");
                    return ObstacleFinder.BLOCKED;
                }
            }
            if (c.view.isOccupied(cx, cy - dy) || c.view.isOccupied(cx - dx, cy)) {
                c.append(cx, cy, "b");
                return ObstacleFinder.BLOCKED;
            }
            if (c.goals.contains(cx * c.h + cy)) {
                c.append(cx, cy, "g");
                return new JpsNode(p, cx, cy, ObstacleType.GOAL);
            }
            if (dx == dy) {
                if (cx + 1 < c.bounds.xmax && cy - 1 >= c.bounds.ymin && c.view.isOccupied(cx + 1, cy - 1)) {
                    c.append(cx, cy, "d");
                    return new JpsNode(p, cx, cy, ObstacleType.FORCED_NEIGHBOR_DIAG);
                }
                if (cx - 1 >= c.bounds.xmin  && cy + 1 < c.bounds.ymax && c.view.isOccupied(cx - 1, cy + 1)) {
                    c.append(cx, cy, "d");
                    return new JpsNode(p, cx, cy, ObstacleType.FORCED_NEIGHBOR_DIAG);
                }
            } else {
                if (cx - 1 >= c.bounds.xmin && cy - 1 >= c.bounds.ymin && c.view.isOccupied(cx - 1, cy - 1)) {
                    c.append(cx, cy, "d");
                    return new JpsNode(p, cx, cy, ObstacleType.FORCED_NEIGHBOR_DIAG);
                }
                if (cx + 1 < c.bounds.xmax && cy + 1 < c.bounds.ymax && c.view.isOccupied(cx + 1, cy + 1)) {
                    c.append(cx, cy, "d");
                    return new JpsNode(p, cx, cy, ObstacleType.FORCED_NEIGHBOR_DIAG);
                }
            }
            if (searchH(p, c, cx, cy, dx).requiresSearch()) {
                c.append(cx, cy, "h");
                return new JpsNode(p, cx, cy, ObstacleType.FORCED_NEIGHBOR_DIAG);
            }
            if (searchV(p, c, cx, cy, dy).requiresSearch()) {
                c.append(cx, cy, "v");
                return new JpsNode(p, cx, cy, ObstacleType.FORCED_NEIGHBOR_DIAG);
            }
            ++d;
        }
    }

    private static JpsNode search(JpsNode p, JumpPointContext c, int x, int y, int dx, int dy) {
        boolean h = dy == 0;
        boolean v = dx == 0;

        if (!v && !h)
            return searchD(p, c, x, y, dx, dy);
        if (v && h)
            throw new IllegalStateException();
        if (h)
            return searchH(p, c, x, y, dx);
        else
            return searchV(p, c, x, y, dy);
    }

    @Override
    public JpsNode nextObstacle(JpsNode p, JumpPointContext c, int x, int y, int dx, int dy) {
        return search(p, c, x, y, dx, dy);
    }
}
