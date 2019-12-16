package client.gui.mouse;

import client.app.UiClientContext;
import client.gui.game.GamePainter;
import client.gui.game.Zoom;
import client.gui.game.gl.GlListeners;
import client.state.ClientGameState;
import common.msg.Message;
import common.state.Occupancy;
import common.state.spec.EntitySpec;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class BuildingPlacer implements GlListeners.GameMousePressListener {
    private EntitySpec building;
    private int buildingLocX = -1;
    private int buildingLocY = -1;

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
        return new Rectangle(buildingLocX, buildingLocY, building.size.width, building.size.height);
    }

    public boolean isNotPlacing() {
        return building == null || buildingLocX < 0 || buildingLocY < 0;
    }

    public void setBuilding(EntitySpec spec) {
        this.building = spec;
        if (spec == null) {
            buildingLocX = -1;
            buildingLocY = -1;
        }
    }

    private boolean buildBuilding(final EntitySpec building, final int buildingLocX, final int buildingLocY) {
        if (!canBuild()) {
            return false;
        }

        context.executorService.submit(() -> {
            try {
                context.writer.send(new Message.PlaceBuilding(building, buildingLocX, buildingLocY));
                context.writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    public boolean canBuild() {
        return !Occupancy.isOccupied(
                Occupancy.createConstructionOccupancy(context.clientGameState.gameState, context.clientGameState.exploration),
                new Point(buildingLocX, buildingLocY),
                building.size
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
    public void mouseReleased(double x, double y, GlListeners.PressInfo info) {

    }
}
