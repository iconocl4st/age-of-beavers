package client.gui.game;

import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.util.DPoint;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

public interface Zoom {
    void initialize(GameSpec spec, int width, int height);

    // Coords
    void zoom(double amount, double stableX, double stableY);

    void recenter(double cX, double cY);

    double cX();

    double cY();

    double mapGameToScreenX(double x);

    double mapGameToScreenY(double y);

    double mapScreenToGameX(int x);

    double mapScreenToGameY(int y);

    void focusOn(Set<EntityReader> entityIds);

    boolean isOutOfScreen(DPoint p);

    boolean isOutOfScreen(double x, double y);

    boolean isOutOfScreen(double x, double y, double w, double h);

    double getScreenHeight();

    double getScreenWidth();



    // TODO:
    // map a point
    // map a rectangle


    static DPoint averageLocation(Set<EntityReader> entityIds) {
        double x = 0;
        double y = 0;
        int count = 0;
        for (EntityReader entity : entityIds) {
            DPoint location = entity.getLocation();
            if (location == null) continue;
            x += location.x;
            y += location.y;
            count++;
        }
        if (count <= 0) return null;
        return new DPoint(x / count, y / count);
    }


    double getLocationX();

    double getLocationY();

    void drag(int cmX, int smX, int cmY, int smY, double scX, double scY);

    Rectangle2D mapGameToScreen(double x, double y, double w, double h);
    Line2D mapGameLineToScreen(double x1, double y1, double x2, double y2);
    Point2D mapGameToScreen(double x, double y);
    Ellipse2D mapGameCircleToScreen(double x, double y, double r);
    Rectangle2D mapGameToScreen(Rectangle r);
    Line2D mapGameLineToScreen(DPoint sourceCenter, DPoint targetCenter);
    Rectangle2D mapGameEndPointsToScreen(double x1, double y1, double x2, double y2);
    Rectangle mapGameToScreenInts(double gx, double gy, double gwidth, double gheight);
}
