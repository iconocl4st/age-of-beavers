package client.gui.keys;

import client.app.ClientContext;
import common.state.EntityId;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

public class ControlGroupListener implements KeyListener {

    private static final int doublePresSpeed = 300;
    private long lastKeyPressTime;
    private int lastKeyPressedCode;

    private final ClientContext context;

    public ControlGroupListener(ClientContext context) {
        this.context = context;
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
                if (context.uiManager.gameScreen.contextKeyListener.containsKey(KeyEvent.VK_CONTROL)) {
                    context.selectionManager.registerControlGroup(keyEvent.getKeyCode() - KeyEvent.VK_0);
                    return;
                } else {
                    Set<EntityId> entityIds = context.selectionManager.recallControlGroup(keyEvent.getKeyCode() - KeyEvent.VK_0);
                    if (!entityIds.isEmpty() && lastKeyPressedCode == keyEvent.getKeyCode() && now - lastKeyPressTime < doublePresSpeed) {
                        context.uiManager.gameScreen.zoom.focusOn(entityIds);
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
