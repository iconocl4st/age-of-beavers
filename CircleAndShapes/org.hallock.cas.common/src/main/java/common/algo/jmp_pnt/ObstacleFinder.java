package common.algo.jmp_pnt;

import java.util.Collections;

public interface ObstacleFinder {

    JpsNode nextObstacle(JpsNode parent, JumpPointContext context, int x, int y, int dx, int dy);

    JpsNode BLOCKED = new JpsNode(null, -1, -1, Collections.singleton(ObstacleType.BLOCKED));

    enum ObstacleType {
        FORCED_NEIGHBOR_LEFT(true),
        FORCED_NEIGHBOR_RIGHT(true),
        FORCED_NEIGHBOR_UP(true),
        FORCED_NEIGHBOR_DOWN(true),
        FORCED_NEIGHBOR_DIAG(true),
        GOAL(true),
        BLOCKED(false),
        ;

        boolean requiresSearch;

        ObstacleType(boolean requiresSearch) {
            this.requiresSearch = requiresSearch;
        }
    }
}
