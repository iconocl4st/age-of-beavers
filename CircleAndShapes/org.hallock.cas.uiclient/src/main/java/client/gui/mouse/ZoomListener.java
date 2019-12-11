package client.gui.mouse;

import client.gui.game.Zoom;

import javax.swing.*;
import java.awt.event.*;

public class ZoomListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    double scX;
    double scY;
    int smX;
    int smY;
    boolean moving;

    Zoom zoom;
    JPanel panel;

    public ZoomListener(Zoom zoom, JPanel panel) {
        this.zoom = zoom;
        this.panel = panel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        double factor = 0.9;
        int units = mouseWheelEvent.getUnitsToScroll();
        // Don't ask me why it is this:
        double x = zoom.mapScreenToGameX(panel.getWidth() - mouseWheelEvent.getX());
        double y = zoom.mapScreenToGameY(panel.getHeight() - mouseWheelEvent.getY());
        if (units < 0) {
            zoom.zoom(factor, x, y);
        } else if (units > 0) {
            zoom.zoom(1 / factor, x, y);
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (!moving) return;
        int cmX = mouseEvent.getX();
        int cmY = mouseEvent.getY();
        zoom.drag(cmX, smX, cmY, smY, scX,  scY);
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        boolean isMiddleClick = (mouseEvent.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK;
        if (!isMiddleClick)
            return;
        moving = true;
        smX = mouseEvent.getX();
        smY = mouseEvent.getY();
        scX = zoom.cX();
        scY = zoom.cY();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        moving = false;
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        moving = false;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }
}
