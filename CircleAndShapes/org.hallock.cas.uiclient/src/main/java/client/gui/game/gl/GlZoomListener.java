package client.gui.game.gl;

import java.awt.event.*;

class GlZoomListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    private double scX;
    private double scY;
    private int smX;
    private int smY;
    private boolean moving;

    private GlZoom glZoom;

    GlZoomListener(GlZoom glZoom) {
        this.glZoom = glZoom;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        int unitsToScroll = mouseWheelEvent.getUnitsToScroll();
        double factor;
        if (unitsToScroll > 0) {
            factor = 1/(1 - GlConstants.ZOOM_SPEED);
        } else if (unitsToScroll < 0) {
            factor = 1 - GlConstants.ZOOM_SPEED;
        } else {
            return;
        }
        glZoom.locationZ *= factor;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (!moving) return;
        int cmX = mouseEvent.getX();
        int cmY = mouseEvent.getY();

        synchronized (glZoom.sync) {
            double visibleHeight = glZoom.screenUpperY - glZoom.screenLowerY;
            double visibleWidth = glZoom.screenUpperX - glZoom.screenLowerX;
            double pixelsPerTileY = glZoom.screenHeight / visibleHeight;
            double pixelsPerTileX = glZoom.screenWidth / visibleWidth;

            double deltaX = (cmX - smX) / pixelsPerTileX;
            double deltaY = (cmY - smY) / pixelsPerTileY;

            glZoom.locationX = scX + deltaX;
            glZoom.locationY = scY + deltaY;
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        boolean isMiddleClick = (mouseEvent.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK;
        if (!isMiddleClick)
            return;
        moving = true;
        smX = mouseEvent.getX();
        smY = mouseEvent.getY();
        synchronized (glZoom.sync) {
            scX = glZoom.locationX;
            scY = glZoom.locationY;
        }
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
    public void mouseEntered(MouseEvent mouseEvent) {}
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}
    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}
}