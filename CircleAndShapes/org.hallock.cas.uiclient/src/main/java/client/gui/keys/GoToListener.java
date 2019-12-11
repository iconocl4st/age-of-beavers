package client.gui.keys;

import client.app.UiClientContext;
import client.state.SelectionManager;
import common.state.EntityReader;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoToListener implements KeyListener, SelectionManager.SelectionListener {

    private final Set<EntityReader> currentSelection = new HashSet<>();
    private final UiClientContext context;

    public GoToListener(UiClientContext context) {
        this.context = context;
    }

    public void setCurrentSet(Collection<EntityReader> entities) {
        synchronized (currentSelection) {
            currentSelection.clear();
            currentSelection.addAll(entities);
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (currentSelection.isEmpty()) return;
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                context.uiManager.gameScreen.zoom.focusOn(currentSelection);
        }

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}

    @Override
    public void selectionChanged(List<EntityReader> newSelectedUnits) {
        setCurrentSet(newSelectedUnits);
    }
}
