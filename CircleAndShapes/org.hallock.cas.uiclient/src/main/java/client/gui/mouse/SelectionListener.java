package client.gui.mouse;

import client.app.UiClientContext;
import client.gui.game.RectangleListener;
import client.gui.game.Zoom;
import common.state.EntityId;
import common.state.EntityReader;
import common.state.spec.EntitySpec;
import common.util.DPoint;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

public class SelectionListener implements MouseListener, MouseMotionListener {

    double scX;
    double scY;
    int smX;
    int smY;
    boolean moving;
    boolean selecting;
    double sgX;
    double sgY;
    boolean hasDragged;

    private final UiClientContext context;
    public final RectangleListener rectangleListener;
    private final Zoom zoom;
    private final JPanel panel;

    public SelectionListener(UiClientContext context, RectangleListener listener, Zoom zoom, JPanel panel) {
        this.context = context;
        this.rectangleListener = listener;
        this.zoom = zoom;
        this.panel = panel;

        rectangleListener.removeSelectingRectangle();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (!selecting) {
            return;
        }
        hasDragged = true;
        int cmX = mouseEvent.getX();
        int cmY = mouseEvent.getY();
        rectangleListener.setSelectingRectangle(
                sgX,
                sgY,
                zoom.mapScreenToGameX(mouseEvent.getX()),
                zoom.mapScreenToGameY(mouseEvent.getY()),
                smX,
                smY,
                cmX,
                cmY
        );
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        boolean isLeftClick = (mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;
        if (!isLeftClick) {
            selecting = false;
            rectangleListener.removeSelectingRectangle();
            return;
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        hasDragged = false;
        boolean isLeftClick = (mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;

        selecting = isLeftClick && mouseEvent.getClickCount() <= 1;
        if (!selecting) {
            rectangleListener.removeSelectingRectangle();
        } else {
            smX = mouseEvent.getX();
            smY = mouseEvent.getY();
            scX = zoom.cX();
            scY = zoom.cY();
            sgX = zoom.mapScreenToGameX(mouseEvent.getX());
            sgY = zoom.mapScreenToGameY(mouseEvent.getY());
        }

        if (!isLeftClick) return;

        DPoint destination = new DPoint(
                zoom.mapScreenToGameX(mouseEvent.getX()),
                zoom.mapScreenToGameY(mouseEvent.getY())
        );
        Set<EntityReader> entities = context.clientGameState.gameState.locationManager.getEntities(
                destination,
                entity -> !entity.isHidden()
        );
        if (entities.isEmpty()) {
            context.selectionManager.select(entities);
            return;
        }

        if (mouseEvent.getClickCount() <= 1) {
            context.selectionManager.select(entities);
            return;
        }

        if (entities.size() != 1) {
            return;
        }

        EntitySpec type = entities.iterator().next().getType();

        double x1 = zoom.mapScreenToGameX(0);
        double x2 = zoom.mapScreenToGameX(panel.getWidth());
        double y1 = zoom.mapScreenToGameY(0);
        double y2 = zoom.mapScreenToGameY(panel.getHeight());
        context.selectionManager.select(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.max(x1, x2),
                Math.max(y1, y2),
                type
        );
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (selecting && hasDragged) {
            // TODO
            Rectangle2D r = rectangleListener.getGameRectangle();
            context.selectionManager.select(r.getX(), r.getY(), r.getX() + r.getWidth(), r.getY() +  r.getHeight());
        }
        rectangleListener.removeSelectingRectangle();
        moving = false;
        selecting = false;
        hasDragged = false;
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        moving = false;
        selecting = false;
        hasDragged = false;
        rectangleListener.removeSelectingRectangle();
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
    }

    public void removeSelection() {
        rectangleListener.removeSelectingRectangle();
    }

    public boolean isSelecting() {
        return selecting;
    }
}
