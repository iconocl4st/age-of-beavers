package client.gui.game.gl;

import com.jogamp.opengl.glu.GLU;
import common.util.ExecutorServiceWrapper;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;

public class GlRectangleListener implements MouseListener, MouseMotionListener {
    final Object sync = new Object();

    private boolean isSelecting;
    private boolean hasDragged;
    private boolean gameBeginInitialized;

    private int screenXBegin;
    private int screenYBegin;
    private int screenXCurrent;
    private int screenYCurrent;

    double gameXBegin;
    double gameYBegin;
    double gameXCurrent;
    double gameYCurrent;

    private final LinkedList<GlListeners.RectangleHandler> listeners = new LinkedList<>();
    private final ExecutorServiceWrapper service;

    GlRectangleListener(ExecutorServiceWrapper service) {
        this.service = service;
    }

    void addRectangleListener(GlListeners.RectangleHandler recListener) {
        // remove somewhere...
        synchronized (listeners) {
            listeners.add(recListener);
        }
    }

    private void notifyListeners(double xBegin, double yBegin, double xEnd, double yEnd) {
        synchronized (listeners) {
            for (GlListeners.RectangleHandler listener : listeners) {
                service.submit(() -> service.submit(() -> listener.run(xBegin, yBegin, xEnd, yEnd)));
            }
        }
    }

    boolean update(MapToScreenContext cntxt, GLU glu) {
        if (!isSelecting || !hasDragged) return false;

        if (!gameBeginInitialized) {
            cntxt.map(glu, screenXBegin, screenYBegin);
            gameXBegin = cntxt.gameLocationX;
            gameYBegin = cntxt.gameLocationY;
            gameBeginInitialized = true;
        }

        cntxt.map(glu, screenXCurrent, screenYCurrent);
        gameXCurrent = cntxt.gameLocationX;
        gameYCurrent = cntxt.gameLocationY;
        return true;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if ((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)
            return;
        synchronized (sync) {
            isSelecting = true;
            screenXBegin = mouseEvent.getX();
            screenYBegin = mouseEvent.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if ((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)
            return;
        if (!isSelecting || !hasDragged) return;
        synchronized (sync) {
            if (!isSelecting || !hasDragged) return;
            double xBegin = Math.min(gameXBegin, gameXCurrent);
            double yBegin = Math.min(gameYBegin, gameYCurrent);
            double xEnd = Math.max(gameXBegin, gameXCurrent);
            double yEnd = Math.max(gameYBegin, gameYCurrent);
            reset();
            notifyListeners(xBegin, yBegin, xEnd, yEnd);
        }
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        if (!isSelecting) return;
        synchronized (sync) {
            if (!isSelecting) return;
            reset();
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if ((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)
            return;
        if (!isSelecting) return;
        synchronized (sync) {
            if (!isSelecting) return;
            screenXCurrent = mouseEvent.getX();
            screenYCurrent = mouseEvent.getY();
            hasDragged = screenXBegin != screenXCurrent || screenYBegin != screenYCurrent;
        }
    }

    private void reset() {
        gameXBegin = 0;
        gameYBegin = 0;
        gameXCurrent = 0;
        gameYCurrent = 0;
        screenXBegin = 0;
        screenYBegin = 0;
        screenXCurrent = 0;
        screenYCurrent = 0;
        isSelecting = false;
        gameBeginInitialized = false;
        hasDragged = false;
    }


    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}
    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}
    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}

}
