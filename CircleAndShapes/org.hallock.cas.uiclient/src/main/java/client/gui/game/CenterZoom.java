package client.gui.game;

import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.util.DPoint;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

public class CenterZoom implements Zoom, ComponentListener {

    private double centerX;
    private double centerY;
    private double pixelsPerTile;

    private int windowWidth;
    private int windowHeight;

    private Component component;

    CenterZoom(Component component) {
        this.component = component;
    }

    @Override
    public void initialize(GameSpec spec, int width, int height) {
        centerX = spec.width /  2;
        centerY = spec.height / 2;
        pixelsPerTile = 100 * width / spec.width; // TODO: Why multiply by 100 here?
    }

    @Override
    public void zoom(double amount, double stableX, double stableY) {
        centerX = stableX + (centerX - stableX) / amount;
        centerY = stableY + (centerY - stableY) / amount;
        pixelsPerTile /= amount;
    }

    @Override
    public void recenter(double cX, double cY) {
        centerX = cX;
        centerY = cY;
        component.repaint();
    }

    @Override
    public double cX() {
        return centerX;
    }

    @Override
    public double cY() {
        return centerY;
    }

    @Override
    public double mapGameToScreenX(double x) {
        return windowWidth / 2 + pixelsPerTile  * (x - centerX);
    }

    @Override
    public double mapGameToScreenY(double y) {
        return windowHeight / 2 - pixelsPerTile  * (y - centerY);
    }

    @Override
    public double mapScreenToGameX(int x) {
        return centerX + (x - windowWidth / 2.0) / pixelsPerTile;
    }

    @Override
    public double mapScreenToGameY(int y) {
        return centerY - (y - windowHeight / 2.0) / pixelsPerTile;
    }

    @Override
    public void focusOn(Set<EntityReader> entityIds) {
        DPoint dPoint = Zoom.averageLocation(entityIds);
        if (dPoint == null) return;
        centerX = dPoint.x;
        centerY = dPoint.y;
    }

    @Override
    public boolean isOutOfScreen(DPoint p) {
        return isOutOfScreen(p.x, p.y, 0, 0);
    }

    @Override
    public boolean isOutOfScreen(double x, double y) {
        return isOutOfScreen(x, y,0, 0);
    }

    @Override
    public boolean isOutOfScreen(double x, double y, double w, double h) {
        return x + w < centerX - windowWidth / (2 * pixelsPerTile) ||
                w > centerX + windowWidth / (2 * pixelsPerTile) ||
                y + h < centerY - windowHeight / (2 * pixelsPerTile) ||
                y > centerY + windowHeight / (2 * pixelsPerTile);
    }

    @Override
    public double getScreenHeight() {
        return windowHeight / pixelsPerTile;
    }

    @Override
    public double getScreenWidth() {
        return windowWidth / pixelsPerTile;
    }


    @Override
    public Rectangle mapGameToScreenInts(double x, double y, double width, double height) {
        int sX = (int) mapGameToScreenX(x);
        int sY = (int) mapGameToScreenY(y + height);
        return new Rectangle(
                sX,
                sY,
                (int) mapGameToScreenX(x + width) - sX,
                (int) mapGameToScreenY(y) - sY
        );
    }

    @Override
    public Rectangle2D mapGameToScreen(double x, double y, double w, double h) {
        double sX = mapGameToScreenX(x);
        double sY = mapGameToScreenY(y + h);
        return new Rectangle2D.Double(
                sX,
                sY,
                mapGameToScreenX(x + w) - sX,
                mapGameToScreenY(y) - sY
        );
    }

    @Override
    public Ellipse2D mapGameCircleToScreen(double x, double y, double r) {
        double x1 = mapGameToScreenX(x - r);
        double x2 = mapGameToScreenX(x + r);
        double y1 = mapGameToScreenY(x - r);
        double y2 = mapGameToScreenY(x + r);
        return new Ellipse2D.Double(x1, y2, x2 - x1, y1 -  y2);
    }

    @Override
    public Line2D mapGameLineToScreen(double x1, double y1, double x2, double y2) {
        return new Line2D.Double(
            mapGameToScreenX(x1),
            mapGameToScreenY(y1),
            mapGameToScreenX(x2),
            mapGameToScreenY(y2)
        );
    }

    @Override
    public double getLocationX() {
        return centerX - windowWidth / (2.0 * pixelsPerTile);
    }

    @Override
    public double getLocationY() {
        return centerY -  windowHeight / (2.0 * pixelsPerTile);
    }

    @Override
    public void drag(int cmX, int smX, int cmY, int smY, double scX, double scY) {
        double deltaX = (cmX - smX) / (double) windowWidth * getScreenWidth();
        double deltaY = (smY - cmY) / (double) windowHeight * getScreenHeight();
        recenter(scX - deltaX, scY - deltaY);
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        windowWidth = componentEvent.getComponent().getWidth();
        windowHeight = componentEvent.getComponent().getHeight();
    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {}

    @Override
    public void componentShown(ComponentEvent componentEvent) {
        windowWidth = componentEvent.getComponent().getWidth();
        windowHeight = componentEvent.getComponent().getHeight();
    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {}

    @Override
    public Rectangle2D mapGameToScreen(Rectangle r) {
        return mapGameToScreen(r.x, r.y, r.width, r.height);
    }

    @Override
    public Line2D mapGameLineToScreen(DPoint sourceCenter, DPoint targetCenter) {
        return new Line2D.Double(
                mapGameToScreen(sourceCenter.x, sourceCenter.y),
                mapGameToScreen(targetCenter.x, targetCenter.y)
        );
    }

    @Override
    public Rectangle2D mapGameEndPointsToScreen(double x1, double y1, double x2, double y2) {
        return mapGameToScreen(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public Point2D mapGameToScreen(double x, double y) {
        return new Point2D.Double(mapGameToScreenX(x), mapGameToScreenY(y));
    }
}
