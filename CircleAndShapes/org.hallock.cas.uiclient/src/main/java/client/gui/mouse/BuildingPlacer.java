package client.gui.mouse;

import client.app.UiClientContext;
import client.gui.game.gl.GlListeners;
import common.msg.Message;
import common.state.Occupancy;
import common.state.spec.CreationSpec;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class BuildingPlacer implements GlListeners.GameMousePressListener, KeyListener {
    private CreationSpec building;
    private int buildingLocX = -1;
    private int buildingLocY = -1;
    private int currentRotation;

    private final UiClientContext context;

    public BuildingPlacer(UiClientContext context) {
        this.context = context;
    }

    public void setPosition(int x, int y) {
        this.buildingLocX = x;
        this.buildingLocY = y;
    }

    public Rectangle getBuildingLocation() {
        if (isNotPlacing()) return null;
        int x = buildingLocX;
        int y = buildingLocY;
        int w = building.createdType.size.width;
        int h = building.createdType.size.height;
        switch (currentRotation) {
            case 0:
                return new Rectangle(x, y, w, h);
            case 1:
                return new Rectangle(x - h, y, h, w);
            case 2:
                return new Rectangle(x - w, y - h, w, h);
            case 3:
                return new Rectangle(x, y - w, h, w);
            default:
                throw new IllegalStateException();
        }
    }

    public boolean isNotPlacing() {
        return building == null /* || buildingLocX < 0 || buildingLocY < 0 */;
    }

    public void setBuilding(CreationSpec spec) {
        this.building = spec;
        if (spec == null) {
            buildingLocX = -1;
            buildingLocY = -1;
        }
    }

    private boolean buildBuilding(final CreationSpec building, final int buildingLocX, final int buildingLocY) {
        if (!canBuild()) {
            return false;
        }

        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.PlaceBuilding(building, buildingLocX, buildingLocY, currentRotation));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    public boolean canBuild() {
        return buildingLocX < 0 || buildingLocY < 0 || !Occupancy.isOccupied(
                Occupancy.createConstructionOccupancy(context.clientGameState.gameState, context.clientGameState.exploration),
                new Point(buildingLocX, buildingLocY),
                building.createdType.size
        );
    }

    @Override
    public void mousePressed(double x, double y, GlListeners.PressInfo info) {
        if (building == null) return;

        if (info.isRightButton || info.isMiddleButton) {
            building = null;
            buildingLocX = -1;
            buildingLocY = -1;
            return;
        }
        if (!buildBuilding(building, buildingLocX, buildingLocY))
            return;
        if (!info.isControl) {
            building = null;
            buildingLocX = -1;
            buildingLocY = -1;
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_L:
                currentRotation++;
                if (currentRotation >= 4)
                    currentRotation = 0;
        }
    }

    @Override
    public void mouseReleased(double x, double y, GlListeners.PressInfo info) {}

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyReleased(KeyEvent keyEvent) {}
}
