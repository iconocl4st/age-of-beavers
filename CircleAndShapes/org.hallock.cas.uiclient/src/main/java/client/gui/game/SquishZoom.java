package client.gui.game;

import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.util.DPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

public class SquishZoom implements Zoom {

    private double locationX;
    private double locationY;

    private double screenWidth;
    private double screenHeight;

    private final JPanel panel;

    public SquishZoom(JPanel panel) {
        this.panel = panel;
    }

    @Override
    public double getScreenHeight() {
        return screenHeight;
    }

    @Override
    public double getScreenWidth() {
        return screenWidth;
    }

    @Override
    public double getLocationX() {
        return locationX;
    }

    @Override
    public double getLocationY() {
        return locationY;
    }

    @Override
    public void drag(int cmX, int smX, int cmY, int smY, double scX, double scY) {
        double deltaX = (cmX - smX) / (double) panel.getWidth() * getScreenWidth();
        double deltaY = (smY - cmY) / (double) panel.getHeight() * getScreenHeight();
        recenter(scX - deltaX, scY - deltaY);
    }

    @Override
    public Rectangle2D mapGameToScreen(double x, double y, double w, double h) {
        return new Rectangle2D.Double(
            mapGameToScreenX(x),
            mapGameToScreenY(y),
            mapGameToScreenX(x + w) - mapGameToScreenX(x),
            mapGameToScreenY(y) - mapGameToScreenY(y + h)
        );
    }

    @Override
    public Line2D mapGameLineToScreen(double x1, double y1, double x2, double y2) {
        return null;
    }

    @Override
    public Point2D mapGameToScreen(double x, double y) {
        return null;
    }

    @Override
    public Ellipse2D mapGameCircleToScreen(double x, double y, double r) {
        return null;
    }

    @Override
    public Rectangle2D mapGameToScreen(Rectangle r) {
        return null;
    }

    @Override
    public Line2D mapGameLineToScreen(DPoint sourceCenter, DPoint targetCenter) {
        return null;
    }

    @Override
    public Rectangle2D mapGameEndPointsToScreen(double x1, double y1, double x2, double y2) {
        return null;
    }

    @Override
    public Rectangle mapGameToScreenInts(double gx, double gy, double gwidth, double gheight) {
        return null;
    }

    @Override
    public void initialize(GameSpec spec, int width, int height) {
        this.screenWidth = spec.width;
        this.screenHeight = spec.height;
    }

    public void initialize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    // Coords
    @Override
    public void zoom(double amount, double stableX, double stableY) {
        center(
                stableX + (cX() - stableX) / amount,
                stableY + (cY() - stableY) / amount,
                dX() * amount,
                dY() * amount
        );
    }

    @Override
    public void recenter(double cX, double cY) {
        center(cX, cY, dX(), dY());
    }

//    void reScale(double dX, double dY) {
//        center(cX(), cY(), dX, dY);
//    }

    private void center(double cX, double cY, double dX, double dY) {
        locationX = cX - dX;
        locationY = cY - dY;
        screenWidth = 2 * dX;
        screenHeight = 2 * dY;
        // todo
        panel.repaint();
    }

    @Override
    public double cX() {
        return locationX + screenWidth / 2;
    }

    @Override
    public double cY() {
        return locationY + screenHeight / 2;
    }

    double dX() {
        return screenWidth / 2;
    }

    double dY() {
        return screenHeight / 2;
    }

    @Override
    public double mapGameToScreenX(double x) {
        return (int) ((x - locationX) / screenWidth * panel.getWidth());
    }

    @Override
    public double mapGameToScreenY(double y) {
        return panel.getHeight() - (int) ((y - locationY) / screenHeight * panel.getHeight());
    }

    @Override
    public double mapScreenToGameX(int x) {
        return locationX + (x / (double) panel.getWidth()) * screenWidth;
    }

    @Override
    public double mapScreenToGameY(int y) {
        return locationY + ((panel.getHeight() - y) / (double) panel.getHeight()) * screenHeight;
    }


    @Override
    public void focusOn(Set<EntityReader> entityIds) {
        DPoint dPoint = Zoom.averageLocation(entityIds);
        if (dPoint == null) return;
        recenter(dPoint.x, dPoint.y);
    }

    @Override
    public boolean isOutOfScreen(DPoint p) {
        return false;
    }

    @Override
    public boolean isOutOfScreen(double x, double y) {
        return false;
    }

    public boolean isOutOfScreen(double x, double y, double w, double h) {
        return (
                x + w < locationX ||
                        x > locationX + screenWidth ||
                        y + h < locationY ||
                        y > locationY + screenHeight
        );
    }
}
