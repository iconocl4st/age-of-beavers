package client.gui.mouse;

import client.app.ClientContext;
import client.gui.game.GamePainter;
import client.gui.game.Zoom;
import common.msg.Message;
import common.state.spec.EntitySpec;

import java.awt.event.*;
import java.io.IOException;

public class BuildingPlacer implements MouseMotionListener, MouseListener {

    //  TODO: merge with command listener...

    // reduce scope
    public EntitySpec building;
    public int buildingLocX = -1;
    public int buildingLocY = -1;

    private final Zoom zoom;
    private final ClientContext context;

    public BuildingPlacer(ClientContext context, Zoom zoom) {
        this.zoom = zoom;
        this.context = context;
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

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (building == null) return;

        boolean isRightClick = (mouseEvent.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
        boolean isMiddleClick = (mouseEvent.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK;
        boolean isLeftClick = (mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;
        boolean isControlClick = context.uiManager.gameScreen.contextKeyListener.containsKey(KeyEvent.VK_CONTROL);

        if (isRightClick || isMiddleClick) {
            building = null;
            buildingLocX = -1;
            buildingLocY = -1;
            return;
        }
        if (!buildBuilding(building, buildingLocX, buildingLocY))
            return;
        if (!isControlClick) {
            building = null;
            buildingLocX = -1;
            buildingLocY = -1;
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    private boolean buildBuilding(final EntitySpec building, final int buildingLocX, final int buildingLocY) {
        if (GamePainter.any(context.gameState.getOccupancyForAny(), buildingLocX, buildingLocY, building.size.width, building.size.height)) {
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

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        if (building == null) {
            return;
        }
        buildingLocX = (int) zoom.mapScreenToGameX(mouseEvent.getX());
        buildingLocY = (int) zoom.mapScreenToGameY(mouseEvent.getY());
    }
}
