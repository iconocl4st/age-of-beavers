package client.gui.game;

import javax.swing.*;
import java.awt.*;

public interface Renderer {

    void fillRectangle(Color color, double x, double y, double w, double h, double z);
    void fillRectangleEndPoints(Color color, double x1, double y1, double x2, double y2, double z);
    void fillEverything(Color background, double zBrackground);
    void drawLine(Color background, double x1, double y1, double x2, double y2, double z);

    void fillArc(Color color, double centerX, double centerY, double r1, double r2, double beginAngle, double endAngle, double z);

    void fillCircle(Color gatherPoint, double x, double y, double v, double zGatherPoint);

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

        public Graphics2DRenderer(Graphics2D graphics, Zoom zoom, JPanel panel) {
            this.g = graphics;
            this.zoom = zoom;
            this.panel = panel;
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
    }
}
