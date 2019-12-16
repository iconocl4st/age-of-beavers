package client.gui.keys;

import client.app.ClientConstants;
import client.gui.game.Focuser;
import client.gui.game.Zoom;
import client.state.SelectionManager;
import common.state.EntityReader;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

public class ControlGroupListener implements KeyListener {
    private long lastKeyPressTime;
    private int lastKeyPressedCode;

    private final SelectionManager selectionManager;
    private final Focuser focuser;
    private final ContextKeyManager contextKeys;

    public ControlGroupListener(
            SelectionManager selections,
            ContextKeyManager keys,
            Focuser focuser
    ) {
        this.selectionManager = selections;
        this.contextKeys = keys;
        this.focuser = focuser;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        long now = keyEvent.getWhen();
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_0:
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
            case KeyEvent.VK_6:
            case KeyEvent.VK_7:
            case KeyEvent.VK_8:
            case KeyEvent.VK_9:
                if (contextKeys.containsKey(KeyEvent.VK_CONTROL)) {
                    selectionManager.registerControlGroup(keyEvent.getKeyCode() - KeyEvent.VK_0);
                    return;
                } else {
                    Set<EntityReader> entityIds = selectionManager.recallControlGroup(keyEvent.getKeyCode() - KeyEvent.VK_0);
                    if (!entityIds.isEmpty() && lastKeyPressedCode == keyEvent.getKeyCode() && now - lastKeyPressTime < ClientConstants.doublePresSpeed) {
                        focuser.focusOn(Zoom.averageLocation(entityIds));
                    }
                }
                break;
        }

        lastKeyPressTime = now;
        lastKeyPressedCode = keyEvent.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {}
}
