package common.util;

import java.awt.*;

public class Location {
    double centerX;
    double centerY;
    double radiusX;
    double radiusY;
    double orientation;

    double cosOrientation;
    double sinOrientation;

    public Location(DPoint location, Dimension d) {

    }

    public Location(double x,  double y, int w, int  h) {

    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
        cosOrientation = Math.cos(orientation);
        sinOrientation = Math.sin(orientation);
    }

    /*
    [ cos t     -sin t]  [ rx ]
    [ sin t     cos t ]  [ ry ]
    */

    public DPoint getOffset(double dx, double dy) {
        return new DPoint(
                centerX + cosOrientation * dx - sinOrientation * dy,
                centerY + sinOrientation * dx + cosOrientation * dy
        );
    }

    public DPoint lowerLeft() {
        return getOffset(-radiusX, -radiusY);
    }

    public DPoint lowerRight() {
        return getOffset(radiusX, -radiusY);
    }

    public DPoint upperLeft() {
        return getOffset(-radiusX, radiusY);
    }

    public DPoint upperRight() {
        return getOffset(radiusX, radiusY);
    }

//    Set<Point> intersects() {
//
//    }

//    public double minimumDistanceTo(Location other) {
//
//    }

    public boolean project(LocContainer c) {
        boolean moved = false;
        moved |= projectToFace(-sinOrientation, cosOrientation, -sinOrientation * centerX + cosOrientation * centerY + radiusY, c) > 1e-4;
        moved |= projectToFace(sinOrientation, -cosOrientation, sinOrientation * centerX - cosOrientation * centerY - radiusY, c) > 1e-4;
        moved |= projectToFace(-cosOrientation, -sinOrientation, -cosOrientation * centerX - sinOrientation * centerY - radiusX, c) > 1e-4;
        moved |= projectToFace(cosOrientation, sinOrientation, cosOrientation * centerX + sinOrientation * centerY + radiusX, c) > 1e-4;
        return moved;
    }

    public static double projectToFace(double nx, double ny, double b, LocContainer c) {
        double v = c.x * nx + c.y * ny - b;
        if (v <= 0)
            return 0.0;
        c.x -= v * nx;
        c.y -= v * ny;
        return v;
    }

    public String toString() {
        return "";
    }


    private static class LocContainer {
        double x;
        double y;

        public LocContainer() {}

        public LocContainer(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public LocContainer(LocContainer lc) {
            this(lc.x, lc.y);
        }

        public String toString() {
            return x + ", " + y;
        }
    }


    private static void test(double x, double y, double b, double px, double py) {
        LocContainer lc = new LocContainer(px, py);
        lc.x = px;
        lc.y = py;
        LocContainer original = new LocContainer(lc);
        double n = Math.sqrt(x * x + y * y);
        System.out.println(projectToFace(x / n, y / n, b, lc));
        double dx = original.x - lc.x;
        double dy = original.y - lc.y;
        System.out.println(Math.sqrt(dx * dx + dy * dy));

        System.out.println(lc);

        System.out.println("========================");
    }

    public static void main(String[] args) {
        test(1, 0, 0, 1, 1);
        test(0, 1, 0, 1, 1);
        test(1, 1, -1, 1, 1);
        test(1, 1, -1, -2, -2);
        test(1, 1, -1, -0.7071067811865472, -0.7071067811865472);
    }
}
