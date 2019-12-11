package common.algo.jmp_pnt;

public class RequiredSearches {
    int[] dxs = new int[8];
    int[] dys = new int[8];
    int numSearches;

    private void add(int dx, int dy) {
        if (dx == 0 && dy == 0) return;

        for (int i = 0; i < numSearches; i++) {
            if (dxs[i] == dx && dys[i] == dy)
                return;
        }
        dxs[numSearches] = dx; dys[numSearches] = dy; ++numSearches;
    }

    void setRequiredSearches(JpsNode current) {
        if (current.parent == null) {
            searchEveryWhere();
            return;
        }

        int dx = current.x - current.parent.x; if (dx != 0) dx /= Math.abs(dx);
        int dy = current.y - current.parent.y; if (dy != 0) dy /= Math.abs(dy);


        numSearches = 0;
        for (ObstacleFinder.ObstacleType type : current.types) {
            switch (type) {
                case FORCED_NEIGHBOR_DIAG:
                    add(dx, dy);
                    add(0, dy);
                    add(dx, 0);
                    break;
                case FORCED_NEIGHBOR_UP:
                    add(0, 1);
                    add(dx, 1);
                    add(dx, 0);
                    break;
                case FORCED_NEIGHBOR_DOWN:
                    add(0, -1);
                    add(dx, -1);
                    add(dx, 0);
                    break;
                case FORCED_NEIGHBOR_LEFT:
                    add(-1, 0);
                    add(-1, dy);
                    add(0, dy);
                    break;
                case FORCED_NEIGHBOR_RIGHT:
                    add(1, 0);
                    add(1, dy);
                    add(0, dy);
                    break;
                case BLOCKED:
                    break;
                case GOAL:
                    System.out.println("here.");
                default:
                    throw new IllegalStateException(type.name());
            }
        }
    }

    private void searchEveryWhere() {
        numSearches = 0;
        dxs[numSearches] = -1; dys[numSearches] = -1; ++numSearches;
        dxs[numSearches] = -1; dys[numSearches] = +0; ++numSearches;
        dxs[numSearches] = -1; dys[numSearches] = +1; ++numSearches;
        dxs[numSearches] = +0; dys[numSearches] = -1; ++numSearches;
        dxs[numSearches] = +0; dys[numSearches] = +1; ++numSearches;
        dxs[numSearches] = +1; dys[numSearches] = -1; ++numSearches;
        dxs[numSearches] = +1; dys[numSearches] = +0; ++numSearches;
        dxs[numSearches] = +1; dys[numSearches] = +1; ++numSearches;
    }
}
