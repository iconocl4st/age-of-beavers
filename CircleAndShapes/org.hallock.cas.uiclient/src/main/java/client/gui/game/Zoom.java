package client.gui.game;

import client.app.UiClientContext;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.GameSpec;
import common.util.DPoint;

import javax.swing.*;
import java.util.Set;

public class Zoom {
    public double locationX;
    public double locationY;

    public double screenWidth;
    public double screenHeight;

    JPanel panel;

    // todo: remove
    UiClientContext context;

    public Zoom(JPanel panel, UiClientContext context) {
        this.panel = panel;
        this.context = context;
    }

    public void initialize(GameSpec spec) {
        this.screenWidth = spec.width;
        this.screenHeight = spec.height;
    }

    public void initialize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    // Coords
    public void zoom(double amount, double stableX, double stableY) {
        center(
                stableX + (cX() - stableX) / amount,
                stableY + (cY() - stableY) / amount,
                dX() * amount,
                dY() * amount
        );
    }

    // TODO:
    // map a point
    // map a rectangle

    public void recenter(double cX, double cY) {
        center(cX, cY, dX(), dY());
    }

    void reScale(double dX, double dY) {
        center(cX(), cY(), dX, dY);
    }

    void center(double cX, double cY) {
        center(cX, cY, screenWidth / 2, screenHeight / 2);
    }

    void center(double cX, double cY, double dX, double dY) {
        locationX = cX - dX;
        locationY = cY - dY;
        screenWidth = 2 * dX;
        screenHeight = 2 * dY;
        // todo
        panel.repaint();
    }

    public double cX() {
        return locationX + screenWidth / 2;
    }

    public double cY() {
        return locationY + screenHeight / 2;
    }

    double dX() {
        return screenWidth / 2;
    }

    double dY() {
        return screenHeight / 2;
    }

    public int mapGameToScreenX(double x) {
        return (int) ((x - locationX) / screenWidth * panel.getWidth());
    }

    public int mapGameToScreenY(double y) {
        return panel.getHeight() - (int) ((y - locationY) / screenHeight * panel.getHeight());
    }

    public double mapScreenToGameX(int x) {
        return locationX + (x / (double) panel.getWidth()) * screenWidth;
    }

    public double mapScreenToGameY(int y) {
        return locationY + ((panel.getHeight() - y) / (double) panel.getHeight()) * screenHeight;
    }


    public void focusOn(Set<EntityReader> entityIds) {
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
        if (count <= 0) return;
        center(x / count, y / count);
    }

    boolean isOutOfScreen(double x, double y, double w, double h) {
        return (
                x + w < locationX ||
                        x > locationX + screenWidth ||
                        y + h < locationY ||
                        y > locationY + screenHeight
        );
    }
}
