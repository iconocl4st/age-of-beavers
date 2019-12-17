package server.algo;

import common.util.DPoint;

import java.awt.*;

public class TwoDMapper {
    final Point center;
    final int radius;
    final int w;
    final int h;

    public TwoDMapper(DPoint center, double radius, int w, int h) {
        this.center = center.toPoint();
        this.radius = (int)(radius + 1);
        this.w = w;
        this.h = h;
    }

    int minX() {
        return Math.max(0, center.x - radius);
    }
    int maxX() {
        return Math.min(w, center.x + radius);
    }

    int minY() {
        return Math.max(0, center.y - radius);
    }
    int maxY() {
        return Math.min(h, center.y + radius);
    }

    int mapX(int x) {
        int projGame = Math.max(0, Math.min(w - 1, x));
        int projRec = Math.max(center.x - radius, Math.min(center.x + radius - 1, projGame));
        return projRec - center.x + radius;
    }

    int mapY(int y) {
        int projGame = Math.max(0, Math.min(h - 1, y));
        int projRec = Math.max(center.y - radius, Math.min(center.y + radius - 1, projGame));
        return projRec - center.y + radius;
    }
}
