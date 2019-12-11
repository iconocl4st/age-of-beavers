package common.algo;

import common.CommonConstants;
import common.factory.Path;
import common.factory.SearchDestination;
import common.state.sst.OccupancyView;
import common.util.Bounds;
import common.util.DPoint;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class AStar {
    private static final Path<AStarDebug> FAILED = new Path<>();

//    private static double distance(Point begin, Point end) {
//        double dx = begin.x - end.x;
//        double dy = begin.y - end.y;
//        return Math.sqrt(dx * dx + dy * dy);
//    }
//
//
//    private static Point getClosest(Set<Point> stops, Point end) {
//        Point min = null;
//        for (DPoint p :  stops) {
//            if (min == null || new DPoint(end).distanceTo(p) < new DPoint(end).distanceTo(min)) {
//                min = p;
//            }
//        }
//        return min;
//    }

    public static Path<AStarDebug> aStarSearch(Set<Point> begins, Set<Point> ends, OccupancyView occupied, Bounds bounds, SearchDestination destination) {
        final HashSet<Point> closedSet = new HashSet<>();
        final HashSet<Point> openSet = new HashSet<>();
        final HashMap<Point, Point> cameFrom = new HashMap<>();
        final HashMap<Point, Double> gScore = new HashMap<>();
        final HashMap<Point, Double> fScore = new HashMap<>();

        final PriorityQueue<Point> orderedOpenSet = new PriorityQueue<>(11, (p1, p2) -> {
            Double fScore1 = fScore.get(p1);
            Double fScore2 = fScore.get(p2);
            if (fScore1 == null) {
                return 1;
            }
            if (fScore2 == null) {
                return -1;
            }
            return fScore1.compareTo(fScore2);
        });

        for (Point begin : begins) {
            gScore.put(begin, 0.0);
            fScore.put(begin, DPoint.d(begin, ends));

            openSet.add(begin);
            orderedOpenSet.add(begin);
        }

        while (!openSet.isEmpty()) {
            Point current = orderedOpenSet.poll();
            if (ends.contains(current)) {
                AStarDebug path = postProcess(getPath(cameFrom, current), occupied);
                return new Path<>(new LinkedList<>(path.points), path, AStarDebug.Serializer, destination);
            }

            openSet.remove(current);
            closedSet.add(current);

            for (int dx = -1; dx<2; dx++) {
                for (int dy = -1; dy<2; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }

                    if (bounds.outOfBounds(current.x + dx, current.y + dy)) {
                        continue;
                    }
                    if (occupied.isOccupied(current.x + dx, current.y + dy)) {
                        continue;
                    }
                    if (occupied.isOccupied(current.x, current.y + dy)) {
                        continue;
                    }
                    if (occupied.isOccupied(current.x + dx, current.y)) {
                        continue;
                    }
                    Point neighbor = new Point(current.x + dx, current.y + dy);
                    if (!ends.contains(neighbor) && occupied.isOccupied(neighbor.x, neighbor.y)) {
                        continue;
                    }
                    if (closedSet.contains(neighbor)) {
                        continue;
                    }

                    double d = Math.sqrt(dx * dx + dy * dy);
                    double newGscore = gScore.get(current) + d;
                    Double oldGscore = gScore.get(neighbor);
                    if (oldGscore != null && newGscore >= oldGscore) {
                        continue;
                    }

                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, newGscore);
                    fScore.put(neighbor, newGscore + DPoint.d(neighbor,  ends));

                    openSet.add(neighbor);
                    orderedOpenSet.add(neighbor);
                }
            }
        }

        // Should not have to search every tile for this...
        // This is actually very expensive
        Point minimum = null;
        double minimumFScore = Double.MAX_VALUE;
        for (Map.Entry<Point, Double> entry : fScore.entrySet()) {
            if (entry.getValue() > minimumFScore)
                continue;
            minimum = entry.getKey();
            minimumFScore = entry.getValue();
        }
        if (minimum != null) {
            LinkedList<Point> path = getPath(cameFrom, minimum);
            AStarDebug aStarDebug = postProcess(path, occupied);
            return new Path<>(new LinkedList<>(aStarDebug.points), aStarDebug, AStarDebug.Serializer, destination);
        }

        return FAILED;
    }


    public static final class AStarDebug implements Jsonable {
        public final ArrayList<DPoint> points;
        public final Set<DPoint> intersections = new HashSet<>();
        public final Set<Point> checked = new HashSet<>();
        public final List<Point> originalPoints;

        private AStarDebug(LinkedList<Point> originalPoints) {
            this.originalPoints = (List<Point>) originalPoints.clone();
            this.points = new ArrayList<>(originalPoints.size() + 2);
        }

        AStarDebug() {
            points = new ArrayList<>();
            originalPoints = new LinkedList<>();
        }

        public boolean isEmpty() {
            return points.isEmpty();
        }

        @Override
        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
//            writer.write("post-processed-points", points, DPoint.Serializer, options);
            writer.write("intersection-points", intersections, DPoint.Serializer, options);
            writer.write("checked-locations", checked, DataSerializer.PointSerializer, options);
            writer.write("original-points", originalPoints, DataSerializer.PointSerializer, options);
            writer.writeEndDocument();
        }

        public static final DataSerializer<AStarDebug> Serializer = new DataSerializer.JsonableSerializer<AStarDebug>() {
            @Override
            public AStarDebug parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
                AStarDebug path = new AStarDebug();
                reader.readBeginDocument();
//                reader.read("post-processed-points", points.points, DPoint.Serializer, spec);
                reader.read("intersection-points", path.intersections, DPoint.Serializer, spec);
                reader.read("checked-locations", path.checked, DataSerializer.PointSerializer, spec);
                reader.read("original-points", path.originalPoints, DataSerializer.PointSerializer, spec);
                reader.readEndDocument();
                return path;
            }
        };
    }

    static AStarDebug postProcess(LinkedList<Point> ps, OccupancyView occupied) {
        AStarDebug rp = new AStarDebug(ps);

        rp.points.add(new DPoint(ps.getFirst()));
        for (Point p : ps) {
            rp.points.add(new DPoint(p));
        }
        rp.points.add(new DPoint(ps.getLast()));

        int i = 0;
        while (i + 2 < rp.points.size()) {
            DPoint current = rp.points.get(i);
            DPoint subs = rp.points.get(i+2);
            if (CanTravelBetween.canTravelDirectlyTo4(current, subs, occupied)) {
                rp.points.remove(i+1);
            } else {
                i++;
                if (CommonConstants.PAINT_SEARCH_DEBUG) CanTravelBetween.canTravelDirectlyTo5(rp.intersections, rp.checked, current, rp.points.get(i), occupied);
            }
        }
        if (CommonConstants.PAINT_SEARCH_DEBUG) CanTravelBetween.canTravelDirectlyTo5(rp.intersections, rp.checked, rp.points.get(rp.points.size() - 2), rp.points.get(rp.points.size() - 1), occupied);
        rp.points.trimToSize();
        return rp;
    }

    private static LinkedList<Point> getPath(HashMap<Point, Point> cameFrom, Point end) {
        LinkedList<Point> path = new LinkedList<>();
        path.addLast(end);
        int pdx = 0;
        int pdy = 0;
        while (true) {
            Point current = path.getFirst();
            Point next = cameFrom.get(path.getFirst());
            if (next == null) {
                break;
            }
            int cdx = next.x - current.x;
            int cdy = next.y - current.y;
            if (cdx == pdx && cdy == pdy) {
                path.removeFirst();
            }
            path.addFirst(next);
            pdx = cdx;
            pdy = cdy;
        }
        return path;
    }
}
