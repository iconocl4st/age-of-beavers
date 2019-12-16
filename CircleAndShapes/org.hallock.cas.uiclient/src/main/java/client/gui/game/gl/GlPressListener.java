package client.gui.game.gl;

import client.gui.keys.ContextKeyManager;
import com.jogamp.opengl.glu.GLU;
import common.util.ExecutorServiceWrapper;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.LinkedList;

public class GlPressListener implements MouseListener {
    private final LinkedList<GlListeners.GameMousePressListener> listeners = new LinkedList<>();
    private final LinkedList<ScreenMouseEvent> queue = new LinkedList<>();
    private final ExecutorServiceWrapper service;
    private final ContextKeyManager contextKeys;

    GlPressListener(ExecutorServiceWrapper service, ContextKeyManager contextKeyListener) {
        this.service = service;
        this.contextKeys = contextKeyListener;
    }

    void addPressListener(GlListeners.GameMousePressListener pressListener) {
        // remove somewhere...
        synchronized (listeners) {
            listeners.add(pressListener);
        }
    }

    private void notifyListeners(boolean isPress, double x, double y, GlListeners.PressInfo info) {
        synchronized (listeners) {
            for (GlListeners.GameMousePressListener listener : listeners) {
                service.submit(() -> service.submit(() -> {
                    if (isPress) {
                        listener.mousePressed(x, y, info);
                    } else {
                        listener.mouseReleased(x, y, info);
                    }
                }));
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

            notifyListeners(sme.isPress, gameX, gameY, sme.info);
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        synchronized (queue) {
            queue.addLast(new ScreenMouseEvent(getInfo(mouseEvent), mouseEvent.getX(), mouseEvent.getY(), true));
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        synchronized (queue) {
            queue.addLast(new ScreenMouseEvent(getInfo(mouseEvent), mouseEvent.getX(), mouseEvent.getY(), false));
        }
    }

    private GlListeners.PressInfo getInfo(MouseEvent mouseEvent) {
        return new GlListeners.PressInfo(
                (mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK,
                (mouseEvent.getModifiers() & InputEvent.BUTTON2_MASK) != InputEvent.BUTTON2_MASK,
                (mouseEvent.getModifiers() & InputEvent.BUTTON3_MASK) != InputEvent.BUTTON3_MASK,
                contextKeys.containsKey(KeyEvent.VK_CONTROL),
                contextKeys.containsKey(KeyEvent.VK_SHIFT),
                mouseEvent.getClickCount()
        );
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}
    @Override
    public void mouseExited(MouseEvent mouseEvent) {}
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}


    private static class ScreenMouseEvent {
        final boolean isPress;
        final GlListeners.PressInfo info;
        final int x;
        final int y;

        ScreenMouseEvent(GlListeners.PressInfo info, int x, int y, boolean isPress) {
            this.info = info;
            this.x = x;
            this.y = y;
            this.isPress = isPress;
        }
    }
}
