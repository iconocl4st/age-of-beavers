package common.algo;

import common.CommonConstants;
import common.state.sst.GameState;
import common.util.BitArray;
import common.util.DPoint;
import common.util.json.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class AStar {

    private static double distance(Point begin, Point end) {
        double dx = begin.x - end.x;
        double dy = begin.y - end.y;
        return Math.sqrt(dx * dx + dy * dy);
    }


    private static DPoint getClosest(Set<DPoint> stops, Point end) {
        DPoint min = null;
        for (DPoint p :  stops) {
            if (min == null || new DPoint(end).distanceTo(p) < new DPoint(end).distanceTo(min)) {
                min = p;
            }
        }
        return min;
    }

    private  static HashSet<Point> convert(Set<DPoint> original) {
        HashSet<Point> ends = new HashSet<>();
        for (DPoint end : original) {
            ends.add(end.toPoint());
        }
        return ends;
    }

    public static PathSearch findPath(DPoint start, DPoint stop, GameState.OccupancyView occupied) {
        return findPath(Collections.singleton(start), Collections.singleton(stop), occupied, start);
    }
    public static PathSearch findPath(Set<DPoint> start, DPoint stop, GameState.OccupancyView occupied, DPoint requiredStart) {
        return findPath(start, Collections.singleton(stop), occupied, requiredStart);
    }
    public static PathSearch findPath(DPoint start, Set<DPoint> stops, GameState.OccupancyView occupied) {
        return findPath(Collections.singleton(start), stops, occupied, start);
    }
    public static PathSearch findPath(Set<DPoint> startPoints, Set<DPoint> stops, GameState.OccupancyView occupied, DPoint requiredStart) {
        HashSet<Point> begins = convert(startPoints);
        HashSet<Point> ends = convert(stops);

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
            fScore.put(begin, new DPoint(begin).distanceTo(getClosest(stops, begin)));

            openSet.add(begin);
            orderedOpenSet.add(begin);
        }

        while (!openSet.isEmpty()) {
            Point current = orderedOpenSet.poll();
            if (ends.contains(current)) {
                LinkedList<Point> path = getPath(cameFrom, current);
                return new PathSearch(postProcess(path, occupied, requiredStart));
            }

            openSet.remove(current);
            closedSet.add(current);

            for (int dx = -1; dx<2; dx++) {
                for (int dy = -1; dy<2; dy++) {
                    if (dx == 0 && dy == 0) {
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
                    fScore.put(neighbor, newGscore + new DPoint(neighbor).distanceTo(getClosest(stops, neighbor)));

                    openSet.add(neighbor);
                    orderedOpenSet.add(neighbor);
                }
            }
        }
        return new PathSearch();
    }

    private static double getNext(double d, double c) {
        if (d == 0.0) {
            return Double.NaN;
        }
        double n;
        if (d > 0) {
            n = Math.ceil(c);
            if (n == c) {
                n += 1;
            }
        } else {
            n = Math.floor(c);
            if (n == c) {
                n -= 1;
            }
        }
        return n;
    }


    private static final double epsilon = 1e-8;
    private static boolean canTravelDirectlyTo4(DPoint current, DPoint subs, GameState.OccupancyView occupied) {
        // TODO: only need to check two lines
        // TODO: should use the actual size of the unit...
        Dimension size = new Dimension(1, 1);
        return (
                canTravelDirectlyTo3(new DPoint(current.x, current.y), new DPoint(subs.x, subs.y), occupied) &&
                canTravelDirectlyTo3(new DPoint(current.x + size.width - epsilon, current.y), new DPoint(subs.x + size.width - epsilon, subs.y), occupied) &&
                canTravelDirectlyTo3(new DPoint(current.x, current.y + size.height - epsilon), new DPoint(subs.x, subs.y + size.height - epsilon), occupied) &&
                canTravelDirectlyTo3(new DPoint(current.x + size.width - epsilon, current.y + size.height - epsilon), new DPoint(subs.x + size.width - epsilon, subs.y + size.height - epsilon), occupied)
        );
    }


    private static void canTravelDirectlyTo5(Set<DPoint> intersections, Set<Point> checked, DPoint current, DPoint subs, GameState.OccupancyView occupied) {
        Dimension size = new Dimension(1, 1);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x, current.y), new DPoint(subs.x, subs.y), occupied);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x + size.width - epsilon, current.y), new DPoint(subs.x + size.width - epsilon, subs.y), occupied);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x, current.y + size.height - epsilon), new DPoint(subs.x, subs.y + size.height - epsilon), occupied);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x + size.width - epsilon, current.y + size.height - epsilon), new DPoint(subs.x + size.width - epsilon, subs.y + size.height - epsilon), occupied);
    }

    private static boolean canTravelDirectlyTo3(DPoint begin, DPoint end, GameState.OccupancyView occupied) {
        double[] eqn = getEquation(begin, end);
        if (Math.abs(eqn[0]) > 1e-4) {
            if (moFosOccupied(new double[]{eqn[2], eqn[1], eqn[0]}, Math.min(begin.y, end.y), Math.max(begin.y, end.y), true, occupied)) {
                return false;
            }
        }
        if (Math.abs(eqn[1]) > 1e-4) {
            if (moFosOccupied(new double[]{eqn[2], eqn[0], eqn[1]}, Math.min(begin.x, end.x), Math.max(begin.x, end.x), false, occupied)) {
                return false;
            }
        }
        return true;
    }

    private static boolean moFosOccupied(double[] f, double begin, double end, boolean invert, GameState.OccupancyView occupied) {
        for (int i = (int) Math.ceil(begin); i < Math.ceil(end); i++) {
            double d = (f[0] - f[1] * i) / f[2];
            // checking both is a little bit too conservative
            if (invert) {
                if (occupied.isOccupied((int) Math.floor(d),  i)) return true;
                if (occupied.isOccupied((int) Math.floor(d),  i-1)) return true;
            } else {
                if (occupied.isOccupied(i, (int) Math.floor(d))) return true;
                if (occupied.isOccupied(i - 1, (int) Math.floor(d))) return true;
            }
        }
        return false;
    }

    private static void canTravelDirectlyTo2(Set<DPoint> intersections, Set<Point> checked, DPoint begin, DPoint end, GameState.OccupancyView occupied) {
        double[] eqn = getEquation(begin, end);
        if (Math.abs(eqn[0]) > 1e-4) {
            addDebug(intersections, checked, new double[]{eqn[2], eqn[1], eqn[0]}, Math.min(begin.y, end.y), Math.max(begin.y, end.y), true);
        }
        if (Math.abs(eqn[1]) > 1e-4) {
            addDebug(intersections, checked, new double[]{eqn[2], eqn[0], eqn[1]}, Math.min(begin.x, end.x), Math.max(begin.x, end.x), false);
        }
    }

    private static void addDebug(Set<DPoint> intersections, Set<Point> checked, double[] f, double begin, double end, boolean invert) {
        int first = (int) Math.ceil(begin);
        int last = (int) Math.ceil(end);
        for (int i = first; i < Math.ceil(end); i++) {
            double d = (f[0] - f[1] * i) / f[2];
            if (invert) {
                intersections.add(new DPoint(d, i));
                checked.add(new Point((int) Math.floor(d),  i));
                if (i != first)
                checked.add(new Point((int) Math.floor(d),  i-1));
            } else {
                intersections.add(new DPoint(i, d));
                checked.add(new Point(i, (int) Math.floor(d)));
                if (i != first)
                checked.add(new Point(i - 1, (int) Math.floor(d)));
            }
        }
    }

    private static double[] getEquation(DPoint begin, DPoint end) {
        double a = end.y - begin.y;
        double b = begin.x - end.x;
        double c = a * begin.x + b * begin.y;
        double n = Math.sqrt(a*a + b*b);
        return new double[] {a / n, b / n, c / n};
    }

    private static boolean canTravelDirectlyTo(DPoint begin, DPoint destination, BitArray.BitArrayView occupied) {
        double cX = begin.x;
        double cY = begin.y;

        while (true) {
            double dx = destination.x - cX;
            double dy = destination.y - cY;
            double n = Math.sqrt(dx*dx + dy*dy);
            if (n < 1e-8) {
                return true;
            }

            dx /= n;
            dy /= n;

            double nX = getNext(dx, cX);
            double nY = getNext(dy, cY);

            double nT;
            if (Double.isNaN(nX)) {
                nT = Math.abs((nY - cY) / dy);
            } else if (Double.isNaN(nY)) {
                nT = Math.abs((nX - cX) / dx);
            } else if (Double.isNaN(nX) && Double.isNaN(nY)) {
                return false;
            } else {
                nT = Math.min(
                        Math.abs((nX - cX) / dx),
                        Math.abs((nY - cY) / dy)
                );
            }
            double bX = cX + nT * dx;
            double bY = cY + nT * dy;

            if (Double.isNaN(bX) || Double.isNaN(bY)) {
                System.out.println("?");
            }

            if (occupied.isOutOfBounds((int)Math.floor(bX), (int)Math.floor(bY))) {
                return false;
            }
            if (occupied.get((int)Math.floor(bX), (int)Math.floor(bY))) {
                return false;
            }

            cX = bX;
            cY = bY;
            if (nT >= n) {
                return true;
            }
        }
    }

    public static final class Path implements Jsonable {
        public final ArrayList<DPoint> points;
        public final Set<DPoint> intersections = new HashSet<>();
        public final Set<Point> checked = new HashSet<>();
        public final List<Point> originalPoints;

        private Path(LinkedList<Point> originalPoints) {
            this.originalPoints = (List<Point>) originalPoints.clone();
            this.points = new ArrayList<>(originalPoints.size() + 2);
        }
        private Path() {
            points = new ArrayList<>();
            originalPoints = new LinkedList<>();
        }

        public boolean isEmpty() {
            return points.isEmpty();
        }

        @Override
        public void writeTo(JsonWriterWrapperSpec writer, WriteOptions options) throws IOException {
            writer.writeBeginDocument();
            writer.write("post-processed-points", points, DPoint.Serializer, options);
            writer.write("intersection-points", intersections, DPoint.Serializer, options);
            writer.write("checked-locations", checked, DataSerializer.PointSerializer, options);
            writer.write("original-points", originalPoints, DataSerializer.PointSerializer, options);
            writer.writeEndDocument();
        }

        public static final DataSerializer<Path> Serializer = new DataSerializer.JsonableSerializer<Path>() {
            @Override
            public Path parse(JsonReaderWrapperSpec reader, ReadOptions spec) throws IOException {
                Path path = new Path();
                reader.readBeginDocument();
                reader.read("post-processed-points", path.points, DPoint.Serializer, spec);
                reader.read("intersection-points", path.intersections, DPoint.Serializer, spec);
                reader.read("checked-locations", path.checked, DataSerializer.PointSerializer, spec);
                reader.read("original-points", path.originalPoints, DataSerializer.PointSerializer, spec);
                reader.readEndDocument();
                return path;
            }
        };
    }
    private static Path postProcess(LinkedList<Point> ps, GameState.OccupancyView occupied, DPoint requiredStart) {
        Path rp = new Path(ps);

        if (requiredStart != null) {
            rp.points.add(requiredStart);
        } else {
            rp.points.add(new DPoint(ps.getFirst()));
        }
        for (Point p : ps) {
            rp.points.add(new DPoint(p));
        }
        rp.points.add(new DPoint(ps.getLast()));

        int i = 0;
        while (i + 2 < rp.points.size()) {
            DPoint current = rp.points.get(i);
            DPoint subs = rp.points.get(i+2);
            if (canTravelDirectlyTo4(current, subs, occupied)) {
                rp.points.remove(i+1);
            } else {
                i++;
                if (CommonConstants.PAINT_DEBUG_GRAPHICS) canTravelDirectlyTo5(rp.intersections, rp.checked, current, rp.points.get(i), occupied);
            }
        }
        if (CommonConstants.PAINT_DEBUG_GRAPHICS) canTravelDirectlyTo5(rp.intersections, rp.checked, rp.points.get(rp.points.size() - 2), rp.points.get(rp.points.size() - 1), occupied);
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

    public static final class PathSearch {
        public final boolean successful;
        public final Path path;

        private PathSearch() {
            this.successful = false;
            this.path = new Path(new LinkedList<Point>());
        }
        private PathSearch(Path path) {
            this.successful = true;
            this.path = path;
        }

        public String toString() {
            if (!successful) {
                return "No such path.";
            }
            StringBuilder builder = new StringBuilder();
            for (DPoint p : path.points) {
                builder.append(p.toString());
                builder.append(",");
            }
            return builder.toString();
        }
    }
}
