package common.algo.quad;

import java.awt.*;

public class NearestTracker {
    final int x;
    final int y;

    double minDist = Double.MAX_VALUE;
    Point minimum;

    public NearestTracker(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void consider(Point p) {
        int dx = x - p.x;
        int dy = y - p.y;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d >= minDist) return;
        minDist = d;
        minimum = p;
    }
}
