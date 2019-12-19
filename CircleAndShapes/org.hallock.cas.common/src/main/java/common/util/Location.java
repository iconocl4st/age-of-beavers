package common.util;

import java.awt.*;
import java.util.Set;

public class Location {
    double centerX;
    double centerY;
    double radiusX;
    double radiusY;
    double orientation;

    public Location(DPoint location, Dimension d) {

    }

    public Location(double x,  double y, int w, int  h) {

    }

    public DPoint lowerLeft() {

    }
    public DPoint lowerRight() {

    }
    public DPoint upperLeft() {

    }
    public DPoint upperRight() {
        return new DPoint(
                centerX + 
        );
    }

    Set<Point> intersects() {

    }

    public double minimumDistanceTo(Location other) {

    }

    public String toString() {

    }
}
