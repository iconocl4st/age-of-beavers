package client.gui.keys;

import client.app.ClientContext;
import client.state.SelectionManager;
import common.state.EntityId;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoToListener implements KeyListener, SelectionManager.SelectionListener {

    private final Set<EntityId> currentSelection = new HashSet<>();
    private final ClientContext context;

    public GoToListener(ClientContext context) {
        this.context = context;
    }

    public void setCurrentSet(Collection<EntityId> entities) {
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
    public void selectionChanged(List<EntityId> newSelectedUnits) {
        setCurrentSet(newSelectedUnits);
    }
}
