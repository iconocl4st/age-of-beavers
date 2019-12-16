package client.gui.game;

import client.app.UiClientContext;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public interface Renderer {

    void fillEverything(Color color, double z);

    void fillRectangle(Color color, double x, double y, double w, double h, double z);

    void fillRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z);

    void drawRectangle(Color color, double x, double y, double w, double h, double zSelected);

    void drawLine(Color color, double x1, double y1, double x2, double y2, double z);

    void fillArc(Color color, double centerX, double centerY, double r1, double r2, double beginAngle, double endAngle, double z);

    void fillCircle(Color color, double x, double y, double r, double z);

    void drawProgress(double progress, Color outerColor, Color innerColor, double xCenter, double yCenter, double innerR, double outerR, double zAction);

    void drawGameString(Color color, String str, double x, double y, double z);

    void drawScreenString(Color color, String str, int i, int i1, double z);

    void paintImage(String imagePath, double x, double y, double w, double h, double z);


//    void fillOval(Color color, double x1, double y1, double x2, double y2, double z);
//    void drawOval(Color color, double x1, double y1, double x2, double y2, double z);
//    void drawRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z);
//    void drawRectangle(Color color, double x, double y, double w, double h, double z);
//    void paintImage(String path, double x, double y, double w, double h, double z);
//    void drawString(String string, Color color, double x, double y, double z);


    // fill rectangle
    // fill circle
    // draw line
    // draw string
    // draw progress bar


    class Graphics2DRenderer implements Renderer {
        Graphics2D g;
        Zoom zoom;
        JPanel panel;

        UiClientContext context;

        public Graphics2DRenderer(UiClientContext context, Graphics2D graphics, Zoom zoom, JPanel panel) {
            this.g = graphics;
            this.zoom = zoom;
            this.panel = panel;
            this.context = context;
        }

        public void drawRectangle(Color color, double x1, double y1, double w, double h, double z) {
            g.setColor(color);
            g.draw(zoom.mapGameToScreen(x1, y1, w, h));
        }

        @Override
        public void fillRectangle(Color color, double x1, double y1, double w, double h, double z) {
            g.setColor(color);
            g.fill(zoom.mapGameToScreen(x1, y1, w, h));
        }

        @Override
        public void fillRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z) {
            g.setColor(color);
            g.fill(zoom.mapGameEndPointsToScreen(x1, y1, x2, y2));
        }

        @Override
        public void fillArc(Color color, double centerX, double centerY, double r1, double r2, double beginAngle, double endAngle, double z) {

        }

        @Override
        public void fillCircle(Color gatherPoint, double x, double y, double v, double zGatherPoint) {
            g.setColor(gatherPoint);
            g.fill(zoom.mapGameCircleToScreen(x, y, v));
        }

        @Override
        public void fillEverything(Color background, double zBrackground) {
            g.setColor(Colors.BACKGROUND);
            g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        }

        @Override
        public void drawLine(Color background, double x1, double y1, double x2, double y2, double z) {
            g.setColor(background);
            g.draw(zoom.mapGameLineToScreen(x1, y1, x2, y2));
        }

        @Override
        public void drawProgress(double progress, Color outerColor, Color innerColor, double xCenter, double yCenter, double innerR, double outerR, double zAction) {
            Rectangle2D outer = zoom.mapGameToScreen(xCenter - outerR, yCenter - outerR, 2 * outerR, 2 * outerR);
            Rectangle2D inner = zoom.mapGameToScreen(xCenter - innerR, yCenter - innerR, 2 * innerR, 2 * innerR);
            Area outerArea = new Area(new Ellipse2D.Double(outer.getX(), outer.getY(), outer.getWidth(), outer.getHeight()));
            Area innerArea = new Area(new Ellipse2D.Double(inner.getX(), inner.getY(), inner.getWidth(), inner.getHeight()));
            Area progressArea = new Area(new Arc2D.Double(outer.getX(), outer.getY(), outer.getWidth(), outer.getHeight(), 0, progress * 360, Arc2D.PIE));
            outerArea.subtract(innerArea);
            progressArea.subtract(innerArea);
            g.setColor(innerColor);
            g.fill(outerArea);
            g.setColor(outerColor);
            g.fill(progressArea);
        }

        @Override
        public void drawGameString(Color white, String collect, double x, double y, double zControlGroups) {
            g.setColor(white);
            g.drawString(collect, (int) zoom.mapGameToScreenX(x), (int) zoom.mapGameToScreenY(y));
        }

        @Override
        public void drawScreenString(Color black, String s, int i, int i1, double zLevel) {
            g.setColor(black);
            g.drawString(s, i, i1);
        }

        @Override
        public void paintImage(String imagePath, double gx, double gy, double gwidth, double gheight, double zDisplayable) {
            Rectangle r = zoom.mapGameToScreenInts(gx, gy, gwidth, gheight);

            BufferedImage image = context.imageCache.get(imagePath);
            g.drawImage(
                    image,
                    r.x, r.y,
                    r.x + r.width, r.y + r.height,
                    0, 0,
                    image.getWidth(), image.getHeight(),
                    panel
            );
        }
    }

    class DualRenderer implements Renderer {
        private final Renderer r1;
        private final Renderer r2;

        public DualRenderer(Renderer r1, Renderer r2) {
            this.r1 = r1;
            this.r2 = r2;
        }

        @Override
        public void fillEverything(Color background, double zBrackground) {
            r1.fillEverything(background, zBrackground);
            r2.fillEverything(background, zBrackground);
        }

        @Override
        public void fillRectangle(Color color, double x, double y, double w, double h, double z) {
            r1.fillRectangle(color, x, y, w, h, z);
            r2.fillRectangle(color, x, y, w, h, z);
        }

        @Override
        public void fillRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z) {
            r1.fillRectangleEndPoints(color, x1, y1, x2, y2, z);
            r2.fillRectangleEndPoints(color, x1, y1, x2, y2, z);
        }

        @Override
        public void drawRectangle(Color yellow, double v, double v1, double v2, double v3, double zSelected) {
            r1.drawRectangle(yellow, v, v1, v2, v3, zSelected);
            r2.drawRectangle(yellow, v, v1, v2, v3, zSelected);
        }

        @Override
        public void drawLine(Color background, double x1, double y1, double x2, double y2, double z) {
            r1.drawLine(background, x1, y1, x2, y2, z);
            r2.drawLine(background, x1, y1, x2, y2, z);
        }

        @Override
        public void fillArc(Color color, double centerX, double centerY, double rad1, double rad2, double beginAngle, double endAngle, double z) {
            r1.fillArc(color, centerX, centerY, rad1, rad2, beginAngle, endAngle, z);
            r2.fillArc(color, centerX, centerY, rad1, rad2, beginAngle, endAngle, z);
        }

        @Override
        public void fillCircle(Color gatherPoint, double x, double y, double v, double zGatherPoint) {
            r1.fillCircle(gatherPoint, x, y, v, zGatherPoint);
            r2.fillCircle(gatherPoint, x, y, v, zGatherPoint);
        }

        @Override
        public void drawProgress(double progress, Color outerColor, Color innerColor, double xCenter, double yCenter, double innerR, double outerR, double zAction) {
            r1.drawProgress(progress, outerColor, innerColor, xCenter, yCenter, innerR, outerR, zAction);
            r2.drawProgress(progress, outerColor, innerColor, xCenter, yCenter, innerR, outerR, zAction);
        }

        @Override
        public void drawGameString(Color white, String collect, double x, double y, double zControlGroups) {
            r1.drawGameString(white, collect, x, y, zControlGroups);
            r2.drawGameString(white, collect, x, y, zControlGroups);
        }

        @Override
        public void drawScreenString(Color black, String s, int i, int i1, double zLevel) {
            r1.drawScreenString(black, s, i, i1, zLevel);
            r2.drawScreenString(black, s, i, i1, zLevel);
        }

        @Override
        public void paintImage(String imagePath, double gx, double gy, double gwidth, double gheight, double zDisplayable) {
            r1.paintImage(imagePath, gx, gy, gwidth, gheight, zDisplayable);
            r2.paintImage(imagePath, gx, gy, gwidth, gheight, zDisplayable);
        }
    }

    class EmptyRenderer implements Renderer {
        @Override
        public void fillEverything(Color color, double z) {

        }

        @Override
        public void fillRectangle(Color color, double x, double y, double w, double h, double z) {

        }

        @Override
        public void fillRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z) {

        }

        @Override
        public void drawRectangle(Color color, double x, double y, double w, double h, double zSelected) {

        }

        @Override
        public void drawLine(Color color, double x1, double y1, double x2, double y2, double z) {

        }

        @Override
        public void fillArc(Color color, double centerX, double centerY, double r1, double r2, double beginAngle, double endAngle, double z) {

        }

        @Override
        public void fillCircle(Color color, double x, double y, double r, double z) {

        }

        @Override
        public void drawProgress(double progress, Color outerColor, Color innerColor, double xCenter, double yCenter, double innerR, double outerR, double zAction) {

        }

        @Override
        public void drawGameString(Color color, String str, double x, double y, double z) {

        }

        @Override
        public void drawScreenString(Color color, String str, int i, int i1, double z) {

        }

        @Override
        public void paintImage(String imagePath, double x, double y, double w, double h, double z) {

        }
    }
}
