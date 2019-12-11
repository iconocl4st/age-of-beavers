package common.algo.jmp_pnt;

import common.factory.Path;
import common.factory.SearchDestination;
import common.state.sst.OccupancyView;
import common.util.Bounds;
import common.util.DPoint;
import common.util.json.*;
import common.util.json.DataSerializer.JsonableSerializer;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class JumpPointSearch {
    private static final Path<JPSDebug> FAILED = new Path<>();

//    private static final double SQRT_2 = Math.sqrt(2d);

    private static double d(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static JumpPointContext createContext(OccupancyView view, int w, int h, Set<Point> goals, HashSet<Integer> closedSet, Bounds bounds) {
        JumpPointContext c = new JumpPointContext();
        c.view = view;
        c.w = w;
        c.h = h;

        c.goals = new HashSet<>();
        for (Point g :  goals) {
            c.goals.add(g.x * h + g.y);
        }
        c.gscores = new HashMap<>();
        c.fscores = new HashMap<>();
        c.closedSet = closedSet;
        c.openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> c.fscores.getOrDefault(n.x * h + n.y, 0d)));
        c.obstacleFinder = new SimpleObstacleFinder();
        c.requiredSearches = new RequiredSearches();
        c.debugInfo = new HashMap<>();
        c.bounds = bounds.intersect(Bounds.fromDimension(w, h));
        return c;
    }

    public static Path<JPSDebug> search(OccupancyView view, int w, int h, Set<Point> begins, Set<Point> goals, HashSet<Integer> closedSet, Bounds bounds, SearchDestination destination) {
        JumpPointContext c = createContext(view, w, h, goals, closedSet, bounds);

        JpsNode minimum = null;
        double minimumDistance = Double.MAX_VALUE;

        for (Point p : begins) {
            double d = DPoint.d(p, goals);
            JpsNode o = new JpsNode(p.x, p.y);
            if (d < minimumDistance) {
                minimum = o;
                minimumDistance = d;
            }
            c.gscores.put(p.x * h + p.y, 0d);
            c.fscores.put(p.x * h + p.y, d);
            c.openSet.add(o);
        }

        while (!c.openSet.isEmpty()) {
            JpsNode current = c.openSet.poll();
            if (current == null) throw new IllegalStateException();

            int ndx = current.x * h + current.y;
            if (c.goals.contains(ndx))
                return createPath(c, current, destination);

            c.closedSet.add(ndx);
            double gscore = c.gscores.get(ndx);

            c.requiredSearches.setRequiredSearches(current);
            for (int i = 0; i < c.requiredSearches.numSearches; i++) {
                JpsNode neighbor = c.obstacleFinder.nextObstacle(current, c, current.x, current.y, c.requiredSearches.dxs[i], c.requiredSearches.dys[i]);

                c.append(current.x, current.y, "[" + c.requiredSearches.dxs[i] + "," + c.requiredSearches.dys[i] + "]");
                if (neighbor.types.contains(ObstacleFinder.ObstacleType.BLOCKED)
                    && !neighbor.types.contains(ObstacleFinder.ObstacleType.GOAL)) {
                    c.append(current.x, current.y, "blocked");
                    continue;
                }

                int nindex = neighbor.x * h + neighbor.y;
                if (c.closedSet.contains(nindex)) {
                    c.append(current.x, current.y, "closed");
                    continue;
                }

                Double prevGScore = c.gscores.get(nindex);
                double newGScore = gscore + d(current.x, current.y, neighbor.x, neighbor.y);
                if (prevGScore != null && newGScore >= prevGScore) {
                    c.append(current.x, current.y, "exp");
                    continue;
                }
                double d = DPoint.d(neighbor.x, neighbor.y, goals);
                c.gscores.put(nindex, newGScore);
                c.fscores.put(nindex, newGScore + d);

                c.openSet.add(neighbor);
                c.append(current.x, current.y, "added");

                if  (d < minimumDistance) {
                    minimum = neighbor;
                    minimumDistance = d;
                }
            }
        }
        if (minimum == null)
            return FAILED;

        // This only goes to the nearest search point, which could be far away...
        return createPath(c, minimum, destination);
    }

    private static Path<JPSDebug> createPath(JumpPointContext c, JpsNode current, SearchDestination destination) {
        List<Point> path = getPath(new LinkedList<>(), current);

        Map<Point, String> m = new HashMap<>();
        for (Map.Entry<Integer, String> ndx : c.debugInfo.entrySet())
            m.put(new Point(ndx.getKey() / c.h, ndx.getKey() % c.h), ndx.getValue());
        List<Point> foundPath = new LinkedList<>(path);
        return new Path<>(DPoint.convert(prunePath(path)), new JPSDebug(foundPath, m), JPSDebug.Serializer, destination);

    }

    private static List<Point> getPath(LinkedList<Point> currentPath, JpsNode current) {
        if (current == null) return currentPath;
        getPath(currentPath, current.parent);
        currentPath.addLast(new Point(current.x, current.y));
        return currentPath;
    }

    private static List<Point> prunePath(List<Point> currentPath) {
        if (currentPath.size() < 3)
            return currentPath;
        List<Point> toRemove = new LinkedList<>();
        Iterator<Point> iterator = currentPath.iterator();
        Point p;
        Point c = iterator.next();
        Point n = iterator.next();
        while (iterator.hasNext()) {
            p = c;
            c = n;
            n = iterator.next();

            if (Integer.compare(c.x - p.x, 0) == Integer.compare(n.x - c.x, 0) && Integer.compare(c.y - p.y, 0) == Integer.compare(n.y - c.y, 0))
                toRemove.add(c);
        }
        currentPath.removeAll(toRemove);
        return currentPath;
    }


    public static class JPSDebug implements Jsonable {
        public final Map<Point, String> closedSet;
        public final Collection<Point> foundPath;

        JPSDebug(Collection<Point> foundPath, Map<Point, String> closedSet) {
            this.closedSet = closedSet;
            this.foundPath = foundPath;
        }

        @Override
        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("debug-points", closedSet, DataSerializer.PointSerializer, DataSerializer.StringSerializer, options);
            writer.write("original-points", foundPath, DataSerializer.PointSerializer, options);
            writer.writeEndDocument();
        }


        public static final JsonableSerializer<JPSDebug> Serializer = new JsonableSerializer<JPSDebug>(){
            @Override
            public JPSDebug parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
                reader.readBeginDocument();
                Map<Point, String> closedSet = reader.read("debug-points", new HashMap<>(), DataSerializer.PointSerializer, DataSerializer.StringSerializer, spec);
                Collection<Point> foundPath = reader.read("original-points", new HashSet<>(), DataSerializer.PointSerializer, spec);
                reader.readEndDocument();
                return new JPSDebug(foundPath, closedSet);
            }
        };
    }
}
