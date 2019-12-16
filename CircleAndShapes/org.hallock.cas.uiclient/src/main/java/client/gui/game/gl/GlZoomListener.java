package client.gui.game.gl;

import com.jogamp.opengl.glu.GLU;

import java.awt.event.*;
import java.util.Collection;
import java.util.LinkedList;

class GlZoomListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    private static final boolean STABLE_ZOOM = true;



    private double scX;
    private double scY;
    private int smX;
    private int smY;
    private boolean moving;

    private final LinkedList<ZoomEvent> zoomEvents = new LinkedList<>();
    private GlZoom glZoom;

    GlZoomListener(GlZoom glZoom) {
        this.glZoom = glZoom;
    }

    void update(MapToScreenContext mapContext, GLU glu) {
        Collection<ZoomEvent> evnts;
        synchronized (zoomEvents) {
            evnts = (Collection<ZoomEvent>) zoomEvents.clone();
            zoomEvents.clear();
        }
        synchronized (glZoom.sync) {
            for (ZoomEvent ze : evnts) {
                mapContext.getRay(glu, ze.x, ze.y);

                double desiredZ = glZoom.locationZ * ze.factor;
                double t = (desiredZ - glZoom.locationZ) / mapContext.gameRayZ;

                glZoom.locationX -= t * mapContext.gameRayX;
                glZoom.locationY -= t * mapContext.gameRayY;
                glZoom.locationZ += t * mapContext.gameRayZ;
            }
        }
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
        synchronized (zoomEvents) {
            if (zoomEvents.isEmpty())
                zoomEvents.addLast(new ZoomEvent(mouseWheelEvent.getX(), mouseWheelEvent.getY(), factor));
        }
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

            glZoom.locationX = scX - deltaX;
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



    private static final class ZoomEvent {
        private final int x;
        private final int y;
        private final double factor;

        private ZoomEvent(int x, int y, double factor) {
            this.x = x;
            this.y = y;
            this.factor = factor;
        }
    }
}