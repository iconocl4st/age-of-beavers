package common.algo;

import common.state.sst.OccupancyView;
import common.util.BitArray;
import common.util.DPoint;

import java.awt.*;
import java.util.Set;

public class CanTravelBetween {
    private static final double epsilon = 1e-8;

    static boolean canTravelDirectlyTo4(DPoint current, DPoint subs, OccupancyView occupied) {
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

    static void canTravelDirectlyTo5(Set<DPoint> intersections, Set<Point> checked, DPoint current, DPoint subs, OccupancyView occupied) {
        Dimension size = new Dimension(1, 1);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x, current.y), new DPoint(subs.x, subs.y), occupied);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x + size.width - epsilon, current.y), new DPoint(subs.x + size.width - epsilon, subs.y), occupied);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x, current.y + size.height - epsilon), new DPoint(subs.x, subs.y + size.height - epsilon), occupied);
        canTravelDirectlyTo2(intersections, checked, new DPoint(current.x + size.width - epsilon, current.y + size.height - epsilon), new DPoint(subs.x + size.width - epsilon, subs.y + size.height - epsilon), occupied);
    }

    private static boolean canTravelDirectlyTo3(DPoint begin, DPoint end, OccupancyView occupied) {
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

    private static boolean moFosOccupied(double[] f, double begin, double end, boolean invert, OccupancyView occupied) {
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

    private static void canTravelDirectlyTo2(Set<DPoint> intersections, Set<Point> checked, DPoint begin, DPoint end, OccupancyView occupied) {
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

    private static boolean canTravelDirectlyTo(DPoint begin, DPoint destination, OccupancyView occupied) {
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

            if (occupied.isOccupied((int)Math.floor(bX), (int)Math.floor(bY))) {
                return false;
            }

            cX = bX;
            cY = bY;
            if (nT >= n) {
                return true;
            }
        }
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
}
