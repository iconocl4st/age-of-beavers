package common.algo;

import common.state.sst.OccupancyView;
import common.util.DPoint;

import java.awt.*;
import java.util.*;

public class VisibilityGraph {

    private final Map<Point, Obstacle> obstacles = new HashMap<>();
    public final HashSet<Vertex> vertices = new HashSet<>();



    private void removeDisconnectedVertices() {
        HashSet<Vertex> toRemove = new HashSet<>();

        for (Vertex vertex : vertices) {
            if (vertex.visible.isEmpty())
                toRemove.add(vertex);
        }

        for (Vertex v : toRemove)
            vertices.remove(v);
    }

    private void connectVertices(OccupancyView view) {
        for (Vertex v1 : vertices) {
            for (Vertex v2 : vertices) {
                if (!CanTravelBetween.canTravelDirectlyTo4(new DPoint(v1.location), new DPoint(v2.location), view))
                    continue;
                v1.visible.add(v2);
            }
        }
    }

    private void addObstacle(int w, int h, OccupancyView view, HashSet<Point> processesedObstructions, TreeSet<Point> initial) {
        Obstacle obstacle = new Obstacle();
        while (!initial.isEmpty()) {
            Point p = initial.pollFirst();
            processesedObstructions.add(p);
            obstacles.put(p, obstacle);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int x = p.x + dx;
                    int y = p.y + dy;
                    if (x < 0 || x >= w || y < 0 || y >= h) continue;
                    Point n = new Point(x, y);
                    if (view.isOccupied(x, y)) {
                        if (!processesedObstructions.contains(n)) {
                            initial.add(n);
                        }
                    } else if (dx != 0 && dy != 0) {
                        Vertex vertex = new Vertex(n);
                        vertices.add(vertex);
                        obstacle.vertices.add(vertex);
                    }
                }
            }
        }
    }

    private void addObstacles(int w, int h, OccupancyView view) {
        HashSet<Point> processesedObstructions = new HashSet<>();
        TreeSet<Point> starts = new TreeSet<>(CMP);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (!view.isOccupied(i, j))
                    continue;
                Point p = new Point(i, j);
                if (processesedObstructions.contains(p))
                    continue;
                starts.clear();
                starts.add(p);
                addObstacle(w, h, view, processesedObstructions, starts);
            }
        }
    }

    public static class Vertex {
        public final Point location;
        final HashSet<Vertex> visible = new HashSet<>();

        private Vertex(Point location) {
            this.location = location;
        }

        public int hashCode() {
            return location.hashCode();
        }

        public boolean equals(Object other) {
            if (!(other instanceof Vertex))
                return false;
            Vertex o = (Vertex) other;
            return location.equals(o.location);
        }
    }

    private static class Obstacle {
        final HashSet<Vertex> vertices = new HashSet<>();
    }

    public static VisibilityGraph constructVisibilityGraph(int w, int h, OccupancyView view) {
        VisibilityGraph graph = new VisibilityGraph();
        graph.addObstacles(w, h, view);
        graph.connectVertices(view);
        graph.removeDisconnectedVertices();
        return graph;
    }

    private static Comparator<Point> CMP = (p1, p2) -> {
        int cmp = Integer.compare(p1.x, p2.x);
        if (cmp != 0) return cmp;
        return Integer.compare(p1.y, p2.y);
    };
}
