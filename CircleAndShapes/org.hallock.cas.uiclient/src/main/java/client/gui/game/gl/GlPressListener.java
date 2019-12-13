package client.gui.game.gl;

import com.jogamp.opengl.glu.GLU;
import common.util.ExecutorServiceWrapper;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.LinkedList;

public class GlPressListener implements MouseListener {
    private final LinkedList<GlListeners.GameMousePressListener> listeners = new LinkedList<>();
    private final LinkedList<ScreenMouseEvent> queue = new LinkedList<>();
    private final ExecutorServiceWrapper service;

    GlPressListener(ExecutorServiceWrapper service) {
        this.service = service;
    }

    void addPressListener(GlListeners.GameMousePressListener pressListener) {
        // remove somewhere...
        synchronized (listeners) {
            listeners.add(pressListener);
        }
    }

    private void notifyListeners(double x, double y, GlListeners.PressInfo info) {
        synchronized (listeners) {
            for (GlListeners.GameMousePressListener listener : listeners) {
                service.submit(() -> service.submit(() -> listener.mousePressed(x, y, info)));
            }
        }
    }

    void update(MapToScreenContext cntxt, GLU glu) {
        Collection<ScreenMouseEvent> toProcess;
        synchronized (queue) {
            if (queue.isEmpty()) return;
            toProcess = (Collection<ScreenMouseEvent>) queue.clone();
            queue.clear();
        }
        for (ScreenMouseEvent sme : toProcess) {
            cntxt.map(glu, sme.x, sme.y);
            double gameX = cntxt.gameLocationX;
            double gameY = cntxt.gameLocationY;

            notifyListeners(gameX, gameY, sme.info);
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        synchronized (queue) {
            GlListeners.PressInfo info = new GlListeners.PressInfo(
                    (mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK,
                    (mouseEvent.getModifiers() & InputEvent.BUTTON2_MASK) != InputEvent.BUTTON2_MASK,
                    (mouseEvent.getModifiers() & InputEvent.BUTTON3_MASK) != InputEvent.BUTTON3_MASK
            );
            queue.addLast(new ScreenMouseEvent(info, mouseEvent.getX(), mouseEvent.getY()));
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}
    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}
    @Override
    public void mouseExited(MouseEvent mouseEvent) {}
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}


    private static class ScreenMouseEvent {
        final GlListeners.PressInfo info;
        final int x;
        final int y;

        ScreenMouseEvent(GlListeners.PressInfo info, int x, int y) {
            this.info = info;
            this.x = x;
            this.y = y;
        }
    }
}
