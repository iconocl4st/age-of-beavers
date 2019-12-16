package client.gui.keys;

import common.util.ExecutorServiceWrapper;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ContextKeyManager implements KeyListener {

    private final ExecutorServiceWrapper executor;
    private final HashSet<Integer> currentKeys = new HashSet<>();

    private final List<ContextKeyListener> listeners = new LinkedList<>();

    public ContextKeyManager(ExecutorServiceWrapper executor) {
        this.executor = executor;
    }

    public void addContextKeyListener(ContextKeyListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public boolean containsKey(int keyCode) {
        return currentKeys.contains(keyCode);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_SHIFT:
                if (currentKeys.add(keyEvent.getKeyCode())) {
                    notifyListeners();
                }
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        if (currentKeys.isEmpty()) return;
        currentKeys.clear();
        notifyListeners();
    }

    private void notifyListeners() {
        synchronized (listeners) {
            for (ContextKeyListener listener : listeners) {
                executor.submit(() -> listener.keysChanged(this));
            }
        }
    }

    public interface ContextKeyListener {
        void keysChanged(ContextKeyManager manager);
    }
}
