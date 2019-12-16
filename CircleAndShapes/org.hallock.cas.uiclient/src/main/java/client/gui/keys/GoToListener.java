package client.gui.keys;

import client.gui.game.Focuser;
import client.gui.game.Zoom;
import client.state.SelectionManager;
import common.state.EntityReader;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoToListener implements KeyListener, SelectionManager.SelectionListener {

    private final Set<EntityReader> currentSelection = new HashSet<>();
    private final Focuser focuser;

    public GoToListener(Focuser focuser) {
        this.focuser = focuser;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (currentSelection.isEmpty()) return;
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                synchronized (currentSelection) {
                    focuser.focusOn(Zoom.averageLocation(currentSelection));
                }
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}

    @Override
    public void selectionChanged(List<EntityReader> newSelectedUnits) {
        setCurrentSet(newSelectedUnits);
    }

    public void setCurrentSet(List<EntityReader> entityReaders) {
        synchronized (currentSelection) {
            currentSelection.clear();
            currentSelection.addAll(entityReaders);
        }
    }
}
