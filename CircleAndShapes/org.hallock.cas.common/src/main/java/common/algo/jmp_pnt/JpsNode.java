package common.algo.jmp_pnt;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class JpsNode {
    final int x;
    final int y;
    final Set<ObstacleFinder.ObstacleType> types;
    final JpsNode parent;

    JpsNode(int x, int y) {
        this.parent = null;
        this.x = x;
        this.y = y;
        this.types = Collections.emptySet();
    }

    JpsNode(JpsNode p, int x, int y, Set<ObstacleFinder.ObstacleType> types) {
        this.parent = p;
        this.x = x;
        this.y = y;
        this.types = types;
    }

    JpsNode(JpsNode p, int cx, int y, ObstacleFinder.ObstacleType forcedNeighborUp) {
        this(p, cx, y, Collections.singleton(forcedNeighborUp));
    }

    JpsNode(JpsNode p, int cx, int y, ObstacleFinder.ObstacleType t1, ObstacleFinder.ObstacleType... t2) {
        this(p, cx, y,  new HashSet<>());
        types.add(t1);
        for (ObstacleFinder.ObstacleType t : t2)
            types.add(t);
    }

    public int hashCode() {
        long var1 = java.lang.Double.doubleToLongBits(x);
        var1 ^= java.lang.Double.doubleToLongBits(y) * 31L;
        return (int)var1 ^ (int)(var1 >> 32);
    }

    public boolean equals(Object other) {
        if (!(other instanceof JpsNode)) {
            return false;
        }
        JpsNode n = (JpsNode) other;
        return x == n.x && y == n.y;
    }

    boolean requiresSearch() {
        for (ObstacleFinder.ObstacleType t : types)
            if (t.requiresSearch) return true;
        return false;
    }

    public String toString() {
        return x + ", " + y;
    }
}
